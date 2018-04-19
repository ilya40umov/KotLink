package com.ilya40umov.golink

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class GolinkApplication

fun main(args: Array<String>) {
    runApplication<GolinkApplication>(*args)
}
