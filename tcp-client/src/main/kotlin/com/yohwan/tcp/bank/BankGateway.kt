package com.yohwan.tcp.bank

import org.springframework.integration.annotation.Gateway
import org.springframework.integration.annotation.MessagingGateway

@MessagingGateway
interface BankGateway {

    @Gateway(requestChannel = "bankRequestChannel")
    fun sendTransaction(message: ByteArray): ByteArray
}