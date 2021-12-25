package org.kotlink.ui.help

import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.kotlink.framework.ui.ViewRendererProvider

fun helpRoutes(
    viewRenderer: ViewRendererProvider
): RoutingHttpHandler {
    return routes(
        "/ui/setup_instructions" bind Method.GET to { request ->
            Response(Status.OK).with(
                viewRenderer[request].doRender(
                    template = "help/setup_instructions",
                    data = mapOf(
                        "apiSecret" to "abcdef"
                    )
                )
            )
        }
    )
}