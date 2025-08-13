package com.yohwan.tcp.bank

data class BankCommonHeader(
    val messageTypeCode: Int,
    val messageNumber: Int
)