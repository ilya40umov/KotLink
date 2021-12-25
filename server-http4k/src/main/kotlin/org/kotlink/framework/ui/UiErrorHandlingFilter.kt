package org.kotlink.framework.ui

import mu.KotlinLogging
import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import org.http4k.lens.RequestContextLens
import org.http4k.template.TemplateRenderer
import org.kotlink.domain.alias.AliasNotFoundException
import org.kotlink.framework.exception.BadRequestException
import org.kotlink.framework.oauth.UserPrincipal

private val logger = KotlinLogging.logger {}

class UiErrorHandlingFilter(
    private val templateRenderer: TemplateRenderer,
    private val principal: RequestContextLens<UserPrincipal>
) : Filter {

    override fun invoke(next: HttpHandler): HttpHandler {
        return { request ->
            try {
                next(request)
            } catch (e: BadRequestException) {
                logger.warn(e.message)
                templateRenderer.render400(principal = principal[request])
            }catch (e: AliasNotFoundException) {
                logger.warn(e.message)
                templateRenderer.render404(principal = principal[request])
            } catch (e: Exception) {
                logger.error(e) { "Caught an unexpected exception coming from one of the UI routes." }
                templateRenderer.render500(principal = principal[request])
            }
        }
    }
}