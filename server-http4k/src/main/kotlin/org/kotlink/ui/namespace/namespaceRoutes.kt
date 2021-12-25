package org.kotlink.ui.namespace

import org.http4k.core.Method
import org.http4k.lens.RequestContextLens
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.template.TemplateRenderer
import org.kotlink.domain.namespace.Namespace
import org.kotlink.framework.oauth.UserPrincipal
import org.kotlink.framework.ui.renderView

fun namespaceRoutes(
    templateRenderer: TemplateRenderer,
    principal: RequestContextLens<UserPrincipal>
): RoutingHttpHandler {
    return routes(
        "/ui/namespace" bind Method.GET to { request ->
            templateRenderer.renderView(
                template = "namespace/list",
                principal = principal[request],
                data = mapOf(
                    "namespaces" to emptyList<Namespace>()
                )
            )
        }
    )
}