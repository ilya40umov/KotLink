package org.kotlink

import org.http4k.server.SunHttp
import org.http4k.server.asServer

fun main() {
    logger.info { "Starting KotLink on port ${KotLinkConfig.LOCAL_PORT} for local development." }
    allRoutes(
        config = loadConfig(environment = Environment.LOCAL)
    ).asServer(
        SunHttp(KotLinkConfig.LOCAL_PORT)
    ).start()
}