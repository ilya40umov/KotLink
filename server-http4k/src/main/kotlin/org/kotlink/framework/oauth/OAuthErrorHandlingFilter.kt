package org.kotlink.framework.oauth

import mu.KotlinLogging
import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.kotlink.framework.ui.ViewRendererProvider

private val logger = KotlinLogging.logger {}

class OAuthErrorHandlingFilter(
    private val viewRenderer: ViewRendererProvider
) : Filter {
    override fun invoke(next: HttpHandler): HttpHandler {
        return { request ->
            try {
                val response = next(request)
                if (response.status == Status.FORBIDDEN || response.status == Status.UNAUTHORIZED) {
                    response.with(viewRenderer[request].doRender401())
                } else {
                    response
                }
            } catch (e: Exception) {
                logger.error(e) { "Caught an unexpected exception coming from oauth filters." }
                Response(Status.INTERNAL_SERVER_ERROR)
                    .with(viewRenderer[request].doRender500())
            }
        }
    }
}