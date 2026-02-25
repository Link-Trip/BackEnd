package com.linktrip.bootstrap

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.linktrip"])
class LinktripBootstrapApplication

fun main(args: Array<String>) {
    runApplication<LinktripBootstrapApplication>(*args)
}
