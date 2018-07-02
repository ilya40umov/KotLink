package org.kotlink.ui.namespace

import org.hibernate.validator.constraints.Length
import org.kotlink.core.namespace.Namespace
import javax.validation.constraints.Pattern

class NamespaceUiValue() {

    constructor(namespace: Namespace) : this() {
        id = namespace.id
        keyword = namespace.keyword
        description = namespace.description
    }

    var id: Long = 0

    @get:Pattern(regexp = "[a-z0-9]+")
    @get:Length(min = 1, max = 128)
    var keyword: String = ""

    @get:Length(max = 512)
    var description: String = ""

    fun toNamespace() = Namespace(
        id = id,
        keyword = keyword,
        description = description
    )

    override fun toString(): String {
        return "NamespaceUiValue(id=$id, keyword='$keyword', description='$description')"
    }
}