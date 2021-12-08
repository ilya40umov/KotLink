package org.kotlink

import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.routing.ResourceLoader
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.routing.static
import org.kotlink.framework.thymeleaf.ThymeleafTemplateRenderer
import org.kotlink.ui.alias.aliasRoutes
import org.kotlink.ui.help.helpRoutes
import org.kotlink.ui.namespace.namespaceRoutes

fun allRoutes(config: KotLinkConfig): HttpHandler {
    val templateRenderer = ThymeleafTemplateRenderer(config.hotReload)
    return routes(
        mainRoutes(config.hotReload),
        aliasRoutes(templateRenderer),
        namespaceRoutes(templateRenderer),
        helpRoutes(templateRenderer)
    )
}

fun mainRoutes(hotReload: Boolean): RoutingHttpHandler {
    val resourceLoader = if (hotReload) {
        ResourceLoader.Directory("${Constants.IDE_RESOURCES_DIRECTORY}/static")
    } else {
        ResourceLoader.Classpath("static")
    }
    return routes(
        static(resourceLoader),
        "/" bind Method.GET to {
            Response(Status.TEMPORARY_REDIRECT).header("Location", "/ui/alias")
        }
    )
}