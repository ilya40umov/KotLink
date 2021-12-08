package org.kotlink

import org.http4k.server.SunHttp
import org.http4k.server.asServer
import org.http4k.serverless.ApiGatewayV2LambdaFunction

@Suppress("unused")
class KotLinkFunction : ApiGatewayV2LambdaFunction(allRoutes(KotLinkConfig()))

fun main() {
    allRoutes(KotLinkConfig(hotReload = true)).asServer(SunHttp(9090)).start()
}