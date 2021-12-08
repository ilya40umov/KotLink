package org.kotlink.core.alias

import org.kotlink.core.namespace.Namespace

data class Alias(
    val namespace: Namespace,
    val link: String,
    val redirectUrl: String,
    val description: String
) {
    val fullLink = if (namespace.linkPrefix.isNotEmpty()) "${namespace.linkPrefix} $link" else link
}