package com.yohwan.tcp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TcpApplication

fun main(args: Array<String>) {
	runApplication<TcpApplication>(*args)
}
