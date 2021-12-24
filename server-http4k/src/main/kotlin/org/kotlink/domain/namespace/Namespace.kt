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
    }
}