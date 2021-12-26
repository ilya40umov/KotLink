package org.kotlink.ui.namespace

import org.http4k.core.body.Form
import org.http4k.core.findSingle
import org.kotlink.domain.namespace.Namespace

data class NamespaceForm(
    val namespaceId: String? = null,
    val linkPrefix: String?,
    val description: String?,
    val ownerEmail: String?
) {
    fun linkPrefixPattern() = Namespace.LINK_PREFIX_PATTERN
    fun maxLinkPrefixLength() = Namespace.MAX_LINK_PREFIX_LENGTH
    fun maxDescriptionLength() = Namespace.MAX_DESCRIPTION_LENGTH
    fun maxEmailLength() = Namespace.MAX_EMAIL_LENGTH

    companion object {
        fun empty() = NamespaceForm(
            linkPrefix = "",
            description = "",
            ownerEmail = ""
        )

        fun Namespace.toNamespaceForm() = NamespaceForm(
            namespaceId = id,
            linkPrefix = linkPrefix,
            description = description,
            ownerEmail = ownerEmail
        )

        fun Form.toNamespaceForm() = NamespaceForm(
            linkPrefix = findSingle(NamespaceForm::linkPrefix.name),
            description = findSingle(NamespaceForm::description.name),
            ownerEmail = findSingle(NamespaceForm::ownerEmail.name)
        )
    }
}