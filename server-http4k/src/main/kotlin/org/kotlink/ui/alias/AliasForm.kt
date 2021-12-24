package org.kotlink.ui.alias

import org.http4k.core.body.Form
import org.http4k.core.findSingle
import org.kotlink.domain.alias.Alias

data class AliasForm(
    val aliasId: String? = null,
    val linkPrefix: String?,
    val link: String?,
    val redirectUrl: String?,
    val description: String?,
    val ownerEmail: String?
) {
    fun linkPattern() = Alias.LINK_PATTERN
    fun maxLinkLength() = Alias.MAX_LINK_LENGTH
    fun maxRedirectUrlLength() = Alias.MAX_REDIRECT_URL_LENGTH
    fun maxDescriptionLength() = Alias.MAX_DESCRIPTION_LENGTH
    fun maxEmailLength() = Alias.MAX_EMAIL_LENGTH

    companion object {
        fun empty() = AliasForm(
            linkPrefix = "",
            link = "",
            redirectUrl = "",
            description = "",
            ownerEmail = ""
        )

        fun Alias.toAliasForm() = AliasForm(
            aliasId = id,
            linkPrefix = linkPrefix,
            link = link,
            redirectUrl = redirectUrl,
            description = description,
            ownerEmail = ownerEmail
        )
        
        fun Form.toAliasForm() = AliasForm(
            linkPrefix = findSingle(AliasForm::linkPrefix.name),
            link = findSingle(AliasForm::link.name),
            redirectUrl = findSingle(AliasForm::redirectUrl.name),
            description = findSingle(AliasForm::description.name),
            ownerEmail = findSingle(AliasForm::ownerEmail.name)
        )
    }
}