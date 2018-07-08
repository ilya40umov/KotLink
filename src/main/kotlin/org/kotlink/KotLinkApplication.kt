package org.kotlink

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@EnableCaching
@SpringBootApplication
class KotLinkApplication

fun main(args: Array<String>) {
    runApplication<KotLinkApplication>(*args)
}