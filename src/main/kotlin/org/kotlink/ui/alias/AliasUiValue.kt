package org.kotlink.ui.alias

import org.hibernate.validator.constraints.Length
import org.hibernate.validator.constraints.URL
import org.kotlink.core.alias.Alias
import org.kotlink.core.namespace.Namespace
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern

class AliasUiValue() {

    constructor(alias: Alias): this() {
        id = alias.id
        namespaceId = alias.namespace.id
        link = alias.link
        redirectUrl = alias.redirectUrl
        description = alias.description
    }

    var id: Long = 0

    var namespaceId: Long = 0

    @get:NotBlank
    @get:Pattern(regexp = "[a-z0-9\\s]+")
    @get:Length(max = 128)
    var link: String = ""

    @get:NotBlank
    @get:URL
    @get:Length(max = 2048)
    var redirectUrl: String = ""

    @get:Length(max = 512)
    var description: String = ""

    fun toAlias(namespace: Namespace) = Alias(
        id = id,
        namespace = namespace,
        link = link,
        redirectUrl = redirectUrl,
        description = description
    )

    override fun toString(): String {
        return "AliasUiValue(id=$id, namespaceId=$namespaceId, link='$link', " +
            "redirectUrl='$redirectUrl', description='$description')"
    }
}