package org.kotlink.domain.namespace

data class Namespace(
    val linkPrefix: String,
    val description: String,
    val ownerEmail: String
) {
    val id: String = when {
        linkPrefix.isEmpty() -> DEFAULT_NAMESPACE_ID
        else -> linkPrefix
    }

    companion object {
        const val DEFAULT_NAMESPACE_ID = "__default__"
        const val LINK_PREFIX_PATTERN = "[a-z0-9](?:[a-z0-9\\s]*[a-z0-9])?"
        val LINK_PREFIX_REGEX = LINK_PREFIX_PATTERN.toRegex()
        const val MAX_LINK_PREFIX_LENGTH = 64
        const val MAX_DESCRIPTION_LENGTH = 256
        const val MAX_EMAIL_LENGTH = 256
    }
}