package org.kotlink.api

import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.kotlink.domain.alias.AliasService

fun redirectRoute(aliasService: AliasService): RoutingHttpHandler {
    return routes(
        "/api/link/redirect" bind Method.GET to { request ->
            val link = request.query("link") ?: ""
            val alias = aliasService.findByFullLink(link)
            val redirectUrl = alias?.redirectUrl ?: "/ui/alias?search=$link"
            Response(Status.TEMPORARY_REDIRECT)
                .header("Location", redirectUrl)
        }
    )
}