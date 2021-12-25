package org.kotlink.ui.namespace

import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.kotlink.domain.namespace.Namespace
import org.kotlink.framework.ui.ViewRendererProvider

fun namespaceRoutes(
    viewRenderer: ViewRendererProvider
): RoutingHttpHandler {
    return routes(
        "/ui/namespace" bind Method.GET to { request ->
            Response(Status.OK).with(
                viewRenderer[request].doRender(
                    template = "namespace/list",
                    data = mapOf(
                        "namespaces" to emptyList<Namespace>()
                    )
                )
            )
        }
    )
}