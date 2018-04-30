package org.kotlink.alias

import org.kotlink.namespace.Namespace

/** Represents a memorable link that leads to a certain not-so-memorable URL. */
data class Alias(
    val id: Long,
    val namespace: Namespace,
    val link: String,
    val redirectUrl: String
) {
    val fullLink = if (namespace.keyword.isNotEmpty()) "${namespace.keyword} $link" else link
}