package org.kotlink.core.alias

import org.hibernate.validator.constraints.Length
import org.hibernate.validator.constraints.URL
import org.kotlink.core.account.UserAccount
import org.kotlink.core.namespace.Namespace
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern

/** Represents a memorable link that leads to a certain not-so-memorable URL. */
data class Alias(
    val id: Long,

    val namespace: Namespace,

    @field:NotBlank
    @field:Pattern(regexp = LINK_REGEXP)
    @field:Length(max = MAX_LINK_LENGTH)
    val link: String,

    @field:NotBlank
    @field:URL
    @field:Length(max = MAX_REDIRECT_URL_LENGTH)
    val redirectUrl: String,

    @field:Length(max = MAX_DESCRIPTION_LENGTH)
    val description: String = "",

    val ownerAccount: UserAccount
) {
    val fullLink = if (namespace.keyword.isNotEmpty()) "${namespace.keyword} $link" else link

    companion object {
        const val LINK_REGEXP = "[a-z0-9\\s]+"
        const val MAX_LINK_LENGTH = 128
        const val MAX_REDIRECT_URL_LENGTH = 2048
        const val MAX_DESCRIPTION_LENGTH = 512
    }
}