package org.kotlink.framework.ui

import org.http4k.template.ViewModel
import org.kotlink.framework.oauth.UserPrincipal

class UiViewModel(
    private val template: String,
    val principal: UserPrincipal?,
    val data: Map<String, *>
) : ViewModel {
    override fun template(): String = template
}