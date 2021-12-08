package org.kotlink.ui.namespace

import org.http4k.core.Body
import org.http4k.core.ContentType
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.template.TemplateRenderer
import org.http4k.template.viewModel

fun namespaceRoutes(
    templateRenderer: TemplateRenderer
): RoutingHttpHandler {
    return routes(
        "/ui/namespace" bind Method.GET to {
            val view = Body.viewModel(templateRenderer, ContentType.TEXT_HTML).toLens()
            val model = ListNamespaces(
                namespaces = listOf()
            )
            Response(Status.OK).with(view of model)
        }
    )
}