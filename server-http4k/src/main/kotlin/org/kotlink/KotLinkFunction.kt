package org.kotlink

import mu.KotlinLogging
import org.http4k.serverless.ApiGatewayV2FnLoader
import org.http4k.serverless.ApiGatewayV2LambdaFunction
import org.http4k.serverless.AwsLambdaRuntime
import org.http4k.serverless.asServer

val logger = KotlinLogging.logger {}

/**
 * Implements `com.amazonaws.services.lambda.runtime.RequestStreamHandler`,
 * which is used when deploying to AWS Lambda runtime as a JAR file (i.e. running on JVM).
 */
@Suppress("unused")
class KotLinkFunction : ApiGatewayV2LambdaFunction(
    allRoutes(config = loadConfig(environment = Environment.AWS))
)

/**
 * Used by ShadowJar which gets converted into a GraalVM image.
 */
fun main() {
    logger.info { "Starting KotLink using ApiGatewayV2FnLoader." }
    ApiGatewayV2FnLoader(
        allRoutes(config = loadConfig(environment = Environment.AWS))
    ).asServer(
        AwsLambdaRuntime()
    ).start()
}