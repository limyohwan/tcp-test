package com.yohwan.tcp.bank

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class BankController(private val bankService: BankService) {

    @GetMapping("/api/v1/bank/balance/{accountNumber}")
    fun checkBalanceV1(@PathVariable accountNumber: String): BankResponseMessage {
        val header = BankCommonHeader(
            messageTypeCode = 1001,
            messageNumber = 1
        )
        val message = BankRequestMessage(
            accountNumber = accountNumber
        )
        return bankService.sendTransactionV1(header, message)
    }

    @GetMapping("/api/v2/bank/balance/{accountNumber}")
    fun checkBalanceV2(@PathVariable accountNumber: String): BankResponseMessage {
        val header = BankCommonHeader(
            messageTypeCode = 1001,
            messageNumber = 1
        )
        val message = BankRequestMessage(
            accountNumber = accountNumber
        )
        return bankService.sendTransactionV2(header, message)
    }

    @GetMapping("/api/v3/bank/balance/{accountNumber}")
    fun checkBalanceV3(@PathVariable accountNumber: String): BankResponseMessage {
        val header = BankCommonHeader(
            messageTypeCode = 1001,
            messageNumber = 1
        )
        val message = BankRequestMessage(
            accountNumber = accountNumber
        )
        return bankService.sendTransactionV3(header, message)
    }

}