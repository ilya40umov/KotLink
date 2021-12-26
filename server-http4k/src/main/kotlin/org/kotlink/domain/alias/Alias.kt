package org.kotlink.domain.alias

import org.kotlink.domain.namespace.Namespace

data class Alias(
    val linkPrefix: String,
    val link: String,
    val redirectUrl: String,
    val description: String,
    val ownerEmail: String
) {
    val fullLink = if (linkPrefix.isNotEmpty()) "$linkPrefix $link" else link
    val id: String = computeId(fullLink)

    companion object {
        const val LINK_PATTERN = "[a-z0-9](?:[a-z0-9\\s]*[a-z0-9])?"
        val LINK_REGEX = LINK_PATTERN.toRegex()
        const val MAX_LINK_LENGTH = 64
        const val MAX_REDIRECT_URL_LENGTH = 2048
        const val MAX_DESCRIPTION_LENGTH = Namespace.MAX_DESCRIPTION_LENGTH
        const val MAX_EMAIL_LENGTH = Namespace.MAX_EMAIL_LENGTH

        fun computeId(fullLink: String): String {
            val keywords = fullLink.split("\\s+".toRegex())
            return keywords.sorted().joinToString(separator = "_")
        }
    }
}