package com.ilya40umov.golink

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class GoLinkApplication

fun main(args: Array<String>) {
    runApplication<GoLinkApplication>(*args)
}