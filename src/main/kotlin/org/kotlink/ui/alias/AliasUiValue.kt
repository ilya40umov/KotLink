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
    @get:Pattern(regexp = Alias.LINK_REGEXP)
    @get:Length(max = Alias.MAX_LINK_LENGTH)
    var link: String = ""

    @get:NotBlank
    @get:URL
    @get:Length(max = Alias.MAX_REDIRECT_URL_LENGTH)
    var redirectUrl: String = ""

    @get:Length(max = Alias.MAX_DESCRIPTION_LENGTH)
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