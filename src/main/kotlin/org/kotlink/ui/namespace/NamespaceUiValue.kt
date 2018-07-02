package org.kotlink.ui.namespace

import org.hibernate.validator.constraints.Length
import org.kotlink.core.namespace.Namespace

class NamespaceUiValue() {

    constructor(namespace: Namespace) : this() {
        id = namespace.id
        keyword = namespace.keyword
    }

    var id: Long = 0

    @get:Length(min = 1, max = 128)
    var keyword: String = ""

    override fun toString(): String {
        return "NamespaceUiValue(id=$id, keyword=$keyword)"
    }

    fun toNamespace() = Namespace(
        id = id,
        keyword = keyword
    )
}