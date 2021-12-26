package org.kotlink.framework.ui

import mu.KotlinLogging
import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.kotlink.domain.RecordNotFoundException
import org.kotlink.framework.exception.BadRequestException

private val logger = KotlinLogging.logger {}

class UiErrorHandlingFilter(
    private val viewRenderer: ViewRendererProvider
) : Filter {

    override fun invoke(next: HttpHandler): HttpHandler {
        return { request ->
            try {
                next(request)
            } catch (e: BadRequestException) {
                logger.warn(e.message)
                Response(Status.BAD_REQUEST)
                    .with(viewRenderer[request].doRender400())
            } catch (e: RecordNotFoundException) {
                logger.warn(e.message)
                Response(Status.NOT_FOUND)
                    .with(viewRenderer[request].doRender404())
            } catch (e: Exception) {
                logger.error(e) { "Caught an unexpected exception coming from one of the UI routes." }
                Response(Status.INTERNAL_SERVER_ERROR)
                    .with(viewRenderer[request].doRender500())
            }
        }
    }
}