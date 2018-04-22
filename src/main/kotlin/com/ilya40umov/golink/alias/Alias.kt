package com.ilya40umov.golink.alias

import com.ilya40umov.golink.LINK_SEPARATOR
import com.ilya40umov.golink.namespace.Namespace

/** Represents a memorable link within a namespace that leads to a certain URL. */
data class Alias(
    val id: Int,
    val namespace: Namespace,
    val link: String,
    val redirectUrl: String
) {
    val fullLink = if (namespace.keyword.isNotEmpty()) "${namespace.keyword}$LINK_SEPARATOR$link" else link
}