package com.yohwan.tcp.bank

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/bank")
class BankController(private val bankService: BankService) {

    // 테스트용 편의 메소드들
    @GetMapping("/balance/{accountNumber}")
    fun checkBalance(@PathVariable accountNumber: String): String {
        val header = BankCommonHeader(
            messageTypeCode = 1001,
            messageNumber = 1
        )
        val message = BankRequestMessage(
            accountNumber = accountNumber
        )
        return bankService.sendTransaction(header, message)
    }

}