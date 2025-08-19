package com.yohwan.tcp.bank

import com.yohwan.tcp.bank.BankServerConfig.RESPONSE_SIZE
import com.yohwan.tcp.bank.BankServerConfig.SERVER_HOST
import com.yohwan.tcp.bank.BankServerConfig.SERVER_PORT
import io.netty.bootstrap.Bootstrap
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.timeout.ReadTimeoutHandler
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.IOException
import java.net.Socket
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

@Service
class BankService(
    private val bankGateway: BankGateway
) {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(this::class.java)
    }

    fun sendTransactionV1(header: BankCommonHeader, message: BankRequestMessage): String {
        log.info("sendTransactionV1: use socket")
        return Socket(SERVER_HOST, SERVER_PORT).use { socket ->
            try {
                val requestBuffer = createRequestMessage(
                    header.messageTypeCode.toString(),
                    header.messageNumber.toString(),
                    message.accountNumber
                )

                val out = socket.getOutputStream()
                out.write(requestBuffer.array())
                out.flush()

                val input = socket.getInputStream()

                val bodyResponse = ByteArray(RESPONSE_SIZE)
                input.read(bodyResponse)

                String(bodyResponse, StandardCharsets.US_ASCII)
            } catch (e: IOException) {
                throw RuntimeException("TCP 통신 오류: ${e.message}", e)
            }
        }
    }

    fun sendTransactionV2(header: BankCommonHeader, message: BankRequestMessage): String {
        log.info("sendTransactionV2: use spring integration")
        val requestBuffer = createRequestMessage(
            header.messageTypeCode.toString(),
            header.messageNumber.toString(),
            message.accountNumber
        )

        return String(bankGateway.sendTransaction(requestBuffer.array()), StandardCharsets.US_ASCII)
    }

    fun sendTransactionV3(header: BankCommonHeader, message: BankRequestMessage): String {
        log.info("sendTransactionV3: use netty")
        val requestBuffer = createRequestMessage(
            header.messageTypeCode.toString(),
            header.messageNumber.toString(),
            message.accountNumber
        )

        val group = NioEventLoopGroup()
        val responseFuture = CompletableFuture<ByteArray>()

        try {
            val bootstrap = Bootstrap()
            bootstrap.group(group)
                .channel(NioSocketChannel::class.java)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(object : ChannelInitializer<SocketChannel>() {
                    override fun initChannel(ch: SocketChannel) {
                        val pipeline = ch.pipeline()
                        pipeline.addLast(ReadTimeoutHandler(10, TimeUnit.SECONDS))
                        pipeline.addLast(object : SimpleChannelInboundHandler<ByteBuf>() {

                            private val responseBuf = ByteArray(RESPONSE_SIZE)
                            private var readBytes = 0

                            override fun channelActive(ctx: ChannelHandlerContext) {
                                val buf = Unpooled.wrappedBuffer(requestBuffer.array())
                                ctx.writeAndFlush(buf)
                            }

                            override fun channelRead0(ctx: ChannelHandlerContext, msg: ByteBuf) {
                                val readable = msg.readableBytes()
                                val toRead = minOf(RESPONSE_SIZE - readBytes, readable)
                                msg.readBytes(responseBuf, readBytes, toRead)
                                readBytes += toRead

                                if (readBytes == 50) {
                                    responseFuture.complete(responseBuf)
                                    ctx.close()
                                }
                            }

                            @Suppress("OVERRIDE_DEPRECATION")
                            override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
                                responseFuture.completeExceptionally(cause)
                                ctx.close()
                            }
                        })
                    }
                })

            bootstrap.connect(SERVER_HOST, SERVER_PORT).sync()
            return String(responseFuture.get(10, TimeUnit.SECONDS), StandardCharsets.US_ASCII)
        } finally {
            group.shutdownGracefully()
        }
    }

    fun padLeft(input: String, length: Int, padChar: Char ): String {
        return input.padStart(length, padChar)
    }

    fun padRight(input: String, length: Int, padChar: Char): String {
        return input.padEnd(length, padChar)
    }

    fun createRequestMessage(
        messageTypeCode: String,
        messageNumber: String,
        accountNumber: String
    ): ByteBuffer {
        val messageTypeCodeLength = 4
        val messageNumberLength = 6
        val accountNumberLength = 15

        val paddedMessageTypeCode = padLeft(messageTypeCode, messageTypeCodeLength, '0')
        val paddedMessageNumber = padLeft(messageNumber, messageNumberLength, '0')
        val paddedAccountNumber = padRight(accountNumber, accountNumberLength, ' ')

        val totalLength = messageTypeCodeLength + messageNumberLength + accountNumberLength

        val buffer = ByteBuffer.allocate(totalLength)

        val messageTypeCodeBuffer = ByteBuffer.allocate(messageTypeCodeLength)
        messageTypeCodeBuffer.put(paddedMessageTypeCode.toByteArray(StandardCharsets.US_ASCII))

        val messageNumberBuffer = ByteBuffer.allocate(messageNumberLength)
        messageNumberBuffer.put(paddedMessageNumber.toByteArray(StandardCharsets.US_ASCII))

        val accountNumberBuffer = ByteBuffer.allocate(accountNumberLength)
        accountNumberBuffer.put(paddedAccountNumber.toByteArray(StandardCharsets.UTF_8))

        buffer.put(messageTypeCodeBuffer.array())
        buffer.put(messageNumberBuffer.array())
        buffer.put(accountNumberBuffer.array())

        buffer.flip()

        return buffer
    }
}