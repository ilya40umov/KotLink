package org.kotlink.ui.namespace

import org.hibernate.validator.constraints.Length
import org.kotlink.core.namespace.Namespace
import javax.validation.constraints.Pattern

class NamespaceUiValue() {

    constructor(namespace: Namespace) : this() {
        id = namespace.id
        keyword = namespace.keyword
        description = namespace.description
        ownerAccountEmail = namespace.ownerAccount.email
    }

    var id: Long = 0

    @get:Pattern(regexp = Namespace.KEYWORD_REGEXP)
    @get:Length(min = Namespace.MIN_KEYWORD_LENGTH, max = Namespace.MAX_KEYWORD_LENGTH)
    var keyword: String = ""

    @get:Length(max = Namespace.MAX_DESCRIPTION_LENGTH)
    var description: String = ""

    var ownerAccountEmail: String = ""

    override fun toString(): String {
        return "NamespaceUiValue(id=$id, keyword='$keyword', description='$description', " +
            "ownerAccountEmail='$ownerAccountEmail')"
    }
}