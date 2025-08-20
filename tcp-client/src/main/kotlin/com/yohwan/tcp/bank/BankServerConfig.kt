package com.yohwan.tcp.bank

object BankServerConfig {
    const val SERVER_HOST = "127.0.0.1"
    const val SERVER_PORT = 9999
    const val MESSAGE_TYPE_CODE_LENGTH = 4
    const val MESSAGE_NUMBER_LENGTH = 6
    const val ACCOUNT_NUMBER_LENGTH = 15
    const val BALANCE_LENGTH = 15
    const val RESERVED_LENGTH = 10
    const val RESPONSE_SIZE = MESSAGE_TYPE_CODE_LENGTH + MESSAGE_NUMBER_LENGTH + ACCOUNT_NUMBER_LENGTH + BALANCE_LENGTH + RESERVED_LENGTH
}