package org.kotlink.core.namespace

import org.hibernate.validator.constraints.Length
import javax.validation.constraints.Pattern

/**
 * Groups aliases that start with a common keyword.
 * Namespaces are intended for isolating aliases between teams/projects.
 */
data class Namespace(
    val id: Long = 0,

    @field:Length(min = MIN_KEYWORD_LENGTH, max = MAX_KEYWORD_LENGTH)
    @field:Pattern(regexp = KEYWORD_REGEXP)
    val keyword: String,

    @field:Length(max = MAX_DESCRIPTION_LENGTH)
    val description: String = ""
) {
    companion object {
        const val KEYWORD_REGEXP = "[a-z0-9]+"
        const val MIN_KEYWORD_LENGTH = 1
        const val MAX_KEYWORD_LENGTH = 128
        const val MAX_DESCRIPTION_LENGTH = 512
    }
}