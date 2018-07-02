package org.kotlink.core.namespace

import org.hibernate.validator.constraints.Length
import javax.validation.constraints.Pattern

/**
 * Groups aliases that start with a common keyword.
 * Namespaces are intended for isolating aliases between teams/projects.
 */
data class Namespace(
    val id: Long = 0,
    @field:Length(min = 1, max = 128)
    @field:Pattern(regexp = "[a-z0-9]+")
    val keyword: String,
    @field:Length(max = 512)
    val description: String = ""
)