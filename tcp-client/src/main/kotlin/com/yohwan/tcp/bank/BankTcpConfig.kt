package com.yohwan.tcp.bank

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.annotation.ServiceActivator
import org.springframework.integration.channel.DirectChannel
import org.springframework.integration.ip.tcp.TcpOutboundGateway
import org.springframework.integration.ip.tcp.connection.TcpNetClientConnectionFactory
import org.springframework.integration.ip.tcp.serializer.ByteArrayRawSerializer
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.MessageHandler

@Configuration
class BankTcpConfig {

    companion object {
        private const val SERVER_HOST = "127.0.0.1"
        private const val SERVER_PORT = 9999
    }

    @Bean
    fun bankRequestChannel(): MessageChannel = DirectChannel()

    @Bean
    fun clientConnectionFactory(): TcpNetClientConnectionFactory {
        val factory = TcpNetClientConnectionFactory(SERVER_HOST, SERVER_PORT)
        factory.isSingleUse = true  // 요청마다 소켓을 열고 닫음
        factory.serializer = ByteArrayRawSerializer()
        factory.deserializer = FixedLengthByteArrayDeserializer(50)
        return factory
    }

    @Bean
    @ServiceActivator(inputChannel = "bankRequestChannel")
    fun tcpOutboundGateway(): MessageHandler {
        val gateway = TcpOutboundGateway()
        gateway.setConnectionFactory(clientConnectionFactory())
        return gateway
    }

    @Bean
    fun bankResponseChannel(): MessageChannel = DirectChannel()
}