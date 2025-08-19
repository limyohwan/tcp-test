package com.yohwan.tcp.bank

data class BankResponseMessage(
    val messageTypeCode: Int,
    val messageNumber: Int,
    val accountNumber: String,
    val balance: String,
    val reserved: String
)