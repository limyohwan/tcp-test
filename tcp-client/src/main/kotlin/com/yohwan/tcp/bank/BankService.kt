package com.yohwan.tcp.bank

import org.springframework.stereotype.Service
import java.io.IOException
import java.net.Socket
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

@Service
class BankService {

    companion object {
        private const val SERVER_HOST = "127.0.0.1"
        private const val SERVER_PORT = 9999
    }

    fun sendTransaction(header: BankCommonHeader, message: BankRequestMessage): String {
        return Socket(SERVER_HOST, SERVER_PORT).use { socket ->
            try {
                // 전송 데이터 준비
                val requestBuffer = createRequestMessage(
                    header.messageTypeCode.toString(),
                    header.messageNumber.toString(),
                    message.accountNumber
                )

                // 데이터 전송
                val out = socket.getOutputStream()
                out.write(requestBuffer.array())
                out.flush()

                // 응답 수신
                val input = socket.getInputStream()

                // 본문 읽기
                val bodyResponse = ByteArray(50)
                input.read(bodyResponse)

                String(bodyResponse, StandardCharsets.US_ASCII)
            } catch (e: IOException) {
                throw RuntimeException("TCP 통신 오류: ${e.message}", e)
            }
        }
    }

    fun padLeft(input: String, length: Int, padChar: Char ): String {
        return input.padStart(length, padChar)
    }

    fun padRight(input: String, length: Int, padChar: Char): String {
        return input.padEnd(length, padChar)
    }

    fun createRequestMessage(
        messageTypeCode: String,  // 숫자 4자리
        messageNumber: String,      // 숫자 6자리
        accountNumber: String       // 문자 15자리
    ): ByteBuffer {

        val paddedMessageTypeCode = padLeft(messageTypeCode, 4, '0')
        val paddedMessageNumber = padLeft(messageNumber, 6, '0')
        val paddedAccountNumber = padRight(accountNumber, 15, ' ')

        // 총 메시지 길이 계산 (4 + 6 + 15 = 25)
        val totalLength = 4 + 6 + 15

        val buffer = ByteBuffer.allocate(totalLength)

        val messageTypeCodeBuffer = ByteBuffer.allocate(4)
        messageTypeCodeBuffer.put(paddedMessageTypeCode.toByteArray(StandardCharsets.US_ASCII))

        val messageNumberBuffer = ByteBuffer.allocate(6)
        messageNumberBuffer.put(paddedMessageNumber.toByteArray(StandardCharsets.US_ASCII))

        val accountNumberBuffer = ByteBuffer.allocate(15)
        accountNumberBuffer.put(paddedAccountNumber.toByteArray(StandardCharsets.UTF_8))

        buffer.put(messageTypeCodeBuffer.array())
        buffer.put(messageNumberBuffer.array())
        buffer.put(accountNumberBuffer.array())

        // 버퍼를 읽을 준비 상태로 위치 리셋
        buffer.flip()

        return buffer
    }
}