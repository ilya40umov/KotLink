package org.kotlink.ui.help

import org.http4k.core.Body
import org.http4k.core.ContentType
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.lens.RequestContextLens
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.template.TemplateRenderer
import org.http4k.template.viewModel
import org.kotlink.framework.ui.UiViewModel.Companion.uiViewModel
import org.kotlink.framework.oauth.OAuthPrincipal

fun helpRoutes(
    templateRenderer: TemplateRenderer,
    principal: RequestContextLens<OAuthPrincipal>
): RoutingHttpHandler {
    return routes(
        "/ui/setup_instructions" bind Method.GET to { request ->
            val view = Body.viewModel(templateRenderer, ContentType.TEXT_HTML).toLens()
            Response(Status.OK).with(
                view of request.uiViewModel(
                    template = "help/setup_instructions",
                    data = mapOf(
                        "apiSecret" to "abcdef"
                    ),
                    principal = principal
                )
            )
        }
    )
}