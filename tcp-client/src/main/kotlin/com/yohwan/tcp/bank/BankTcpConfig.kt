package com.yohwan.tcp.bank

import com.yohwan.tcp.bank.BankServerConfig.RESPONSE_SIZE
import com.yohwan.tcp.bank.BankServerConfig.SERVER_HOST
import com.yohwan.tcp.bank.BankServerConfig.SERVER_PORT
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

    @Bean
    fun bankRequestChannel(): MessageChannel = DirectChannel()

    @Bean
    fun clientConnectionFactory(): TcpNetClientConnectionFactory {
        val factory = TcpNetClientConnectionFactory(SERVER_HOST, SERVER_PORT)
        factory.isSingleUse = true  // 요청마다 소켓을 열고 닫음
        factory.serializer = ByteArrayRawSerializer()
        factory.deserializer = FixedLengthByteArrayDeserializer(RESPONSE_SIZE)
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