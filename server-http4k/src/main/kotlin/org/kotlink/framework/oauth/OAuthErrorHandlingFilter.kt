package org.kotlink.framework.oauth

import mu.KotlinLogging
import org.http4k.core.Body
import org.http4k.core.ContentType
import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.template.TemplateRenderer
import org.http4k.template.viewModel
import org.kotlink.framework.ui.UiViewModel
import org.kotlink.framework.ui.render500

private val logger = KotlinLogging.logger {}

class OAuthErrorHandlingFilter(
    private val templateRenderer: TemplateRenderer
) : Filter {
    override fun invoke(next: HttpHandler): HttpHandler {
        return { request ->
            try {
                val response = next(request)
                if (response.status == Status.FORBIDDEN || response.status == Status.UNAUTHORIZED) {
                    val view = Body.viewModel(templateRenderer, ContentType.TEXT_HTML).toLens()
                    response.with(
                        view of UiViewModel(
                            template = "error/401",
                            principal = AnonymousUserPrincipal,
                            data = emptyMap<String, Any>()
                        )
                    )
                } else {
                    response
                }
            } catch (e: Exception) {
                logger.error(e) { "Caught an unexpected exception coming from oauth filters." }
                templateRenderer.render500(principal = AnonymousUserPrincipal)
            }
        }
    }
}