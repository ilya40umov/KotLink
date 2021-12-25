package org.kotlink.ui.help

import org.http4k.core.Method
import org.http4k.lens.RequestContextLens
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.template.TemplateRenderer
import org.kotlink.framework.oauth.UserPrincipal
import org.kotlink.framework.ui.renderView

fun helpRoutes(
    templateRenderer: TemplateRenderer,
    principal: RequestContextLens<UserPrincipal>
): RoutingHttpHandler {
    return routes(
        "/ui/setup_instructions" bind Method.GET to { request ->
            templateRenderer.renderView(
                template = "help/setup_instructions",
                principal = principal[request],
                data = mapOf(
                    "apiSecret" to "abcdef"
                )
            )
        }
    )
}