package org.kotlink

import mu.KotlinLogging
import org.http4k.server.SunHttp
import org.http4k.server.asServer
import org.http4k.serverless.ApiGatewayV2LambdaFunction
import org.kotlink.KotLinkConfig.Companion.LOCAL_PORT

val logger = KotlinLogging.logger {}

@Suppress("unused")
class KotLinkFunction : ApiGatewayV2LambdaFunction(
    allRoutes(
        config = loadConfig(
            environment = Environment.AWS
        )
    )
)

fun main() {
    logger.info { "Starting KotLink on port $LOCAL_PORT for local development." }
    allRoutes(KotLinkConfig(hotReload = true)).asServer(SunHttp(LOCAL_PORT)).start()
}