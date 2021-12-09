package org.kotlink

import mu.KotlinLogging
import org.http4k.server.SunHttp
import org.http4k.server.asServer
import org.http4k.serverless.ApiGatewayV2LambdaFunction

val logger = KotlinLogging.logger {}

@Suppress("unused")
class KotLinkFunction : ApiGatewayV2LambdaFunction(allRoutes(KotLinkConfig()))

fun main() {
    logger.info { "Starting KotLink on port 9090 for local development." }
    allRoutes(KotLinkConfig(hotReload = true)).asServer(SunHttp(9090)).start()
}