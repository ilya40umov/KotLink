package org.kotlink.ui.alias

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
import org.kotlink.core.alias.Alias
import org.kotlink.core.namespace.Namespace

fun aliasRoutes(
    templateRenderer: TemplateRenderer
): RoutingHttpHandler {
    return routes(
        "/ui/alias" bind Method.GET to {
            val input = it.query("input") ?: ""
            val view = Body.viewModel(templateRenderer, ContentType.TEXT_HTML).toLens()
            val model = ListAliases(
                input = input,
                aliases = listOf(
                    Alias(
                        namespace = Namespace(linkPrefix = "", description = "Default namespace."),
                        link = "abc",
                        redirectUrl = "https://abc.com/",
                        description = "Abc.com"
                    ),
                    Alias(
                        namespace = Namespace(linkPrefix = "test", description = "Test namespace."),
                        link = "junit",
                        redirectUrl = "https://junit.org/junit5/",
                        description = "JUnit5 documentation."
                    )
                )
            )
            Response(Status.OK).with(view of model)
        }
    )
}