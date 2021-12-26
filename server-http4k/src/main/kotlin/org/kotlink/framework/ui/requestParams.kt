package org.kotlink.framework.ui

import org.http4k.core.Request
import org.http4k.routing.path

fun Request.searchParam(): String {
    val rawValue = query("search") ?: ""
    return rawValue.lowercase().replace("[^a-z0-9\\s]+".toRegex(), " ").trim()
}

fun Request.idParamFromPath(): String {
    val rawValue = path("id") ?: throw IllegalStateException("No ID parameter in the path")
    return rawValue.lowercase().replace("[^a-z0-9_]+".toRegex(), "_")
}