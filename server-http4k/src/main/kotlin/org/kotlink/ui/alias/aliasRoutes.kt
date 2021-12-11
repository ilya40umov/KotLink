package org.kotlink.ui.alias

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
import org.kotlink.core.alias.Alias
import org.kotlink.core.namespace.Namespace
import org.kotlink.framework.mvc.UiViewModel.Companion.uiViewModel
import org.kotlink.framework.oauth.OAuthPrincipal

fun aliasRoutes(
    templateRenderer: TemplateRenderer,
    principal: RequestContextLens<OAuthPrincipal>
): RoutingHttpHandler {
    return routes(
        "/ui/alias" bind Method.GET to { request ->
            val input = request.query("input") ?: ""
            val view = Body.viewModel(templateRenderer, ContentType.TEXT_HTML).toLens()
            val data = ListAliasesModel(
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
            Response(Status.OK).with(
                view of request.uiViewModel(
                    template = "alias/list",
                    data = data,
                    principal = principal
                )
            )
        },
        "/ui/alias/new" bind Method.GET to { request ->
            val view = Body.viewModel(templateRenderer, ContentType.TEXT_HTML).toLens()
            Response(Status.OK).with(
                view of request.uiViewModel(
                    template = "alias/new",
                    data = null,
                    principal = principal
                )
            )
        }
    )
}