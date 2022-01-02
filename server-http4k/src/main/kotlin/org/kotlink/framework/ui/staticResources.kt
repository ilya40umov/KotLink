package org.kotlink.framework.ui

import org.http4k.routing.ResourceLoader
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.static
import org.kotlink.Constants

fun staticResources(hotReload: Boolean): RoutingHttpHandler {
    val resourceLoader = if (hotReload) {
        ResourceLoader.Directory("${Constants.IDE_RESOURCES_DIRECTORY}/static")
    } else {
        ResourceLoader.Classpath("static")
    }
    return static(resourceLoader)
}