package org.kotlink.api

import mu.KotlinLogging
import org.http4k.core.Body
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Uri
import org.http4k.core.query
import org.http4k.core.with
import org.http4k.format.Jackson
import org.http4k.format.Jackson.json
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.kotlink.domain.alias.Alias
import org.kotlink.domain.alias.AliasService

private val json = Jackson
private val logger = KotlinLogging.logger {}

fun autocompleteRoute(aliasService: AliasService): RoutingHttpHandler {
    fun findStartingWithPrefix(fullLinkPrefix: String): List<Alias> = try {
        aliasService.findStartingWithPrefix(fullLinkPrefix)
    } catch (e: Exception) {
        logger.error(e) { "Failed to find aliases for a prefix: $fullLinkPrefix" }
        emptyList()
    }

    fun getBaseRedirectUri(request: Request): Uri = when (val source = request.source) {
        null -> request.uri.path("/api/link/redirect")
        else -> Uri(
            scheme = source.scheme ?: "http",
            host = source.address,
            port = source.port,
            path = "/api/link/redirect",
            userInfo = "",
            query = "",
            fragment = ""
        )
    }
    return routes(
        "/api/link/suggest" bind Method.GET to { request ->
            // TODO add secret-based authentication
            val link = request.query("link") ?: ""
            val mode = request.query("mode") ?: "simple"
            val redirectUri = getBaseRedirectUri(request)
            val aliases = findStartingWithPrefix(fullLinkPrefix = link)
            val jsonPayload = when (mode) {
                "simple" -> json.array(
                    aliases.map { alias ->
                        json.obj(
                            "first" to json.string(alias.fullLink),
                            "second" to json.string(alias.description)
                        )
                    }
                )
                else -> json.array(
                    listOf(
                        // "opensearch" format
                        // http://www.opensearch.org/Specifications/OpenSearch/Extensions/Suggestions/1.1
                        json.string(link),
                        json.array(aliases.map { json.string(it.fullLink) }),
                        json.array(aliases.map { json.string(it.description) }),
                        json.array(aliases.map { json.string(redirectUri.query("link", it.fullLink).toString()) })
                    )
                )
            }
            Response(Status.OK).with(Body.json().toLens() of jsonPayload)
        }
    )
}