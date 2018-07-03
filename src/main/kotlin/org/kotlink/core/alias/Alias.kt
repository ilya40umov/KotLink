package org.kotlink.core.alias

import org.hibernate.validator.constraints.Length
import org.hibernate.validator.constraints.URL
import org.kotlink.core.namespace.Namespace
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern

/** Represents a memorable link that leads to a certain not-so-memorable URL. */
data class Alias(
    val id: Long,

    val namespace: Namespace,

    @field:NotBlank
    @field:Pattern(regexp = "[a-z0-9\\s]+")
    @field:Length(max = 128)
    val link: String,

    @field:NotBlank
    @field:URL
    @field:Length(max = 2048)
    val redirectUrl: String,

    @field:Length(max = 512)
    val description: String = ""
) {
    val fullLink = if (namespace.keyword.isNotEmpty()) "${namespace.keyword} $link" else link
}