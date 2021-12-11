package org.kotlink.framework.mvc

import org.http4k.core.Request
import org.http4k.lens.RequestContextLens
import org.http4k.template.ViewModel
import org.kotlink.framework.oauth.OAuthPrincipal

class UiViewModel<T>(
    private val template: String,
    val data: T,
    val principal: OAuthPrincipal?
) : ViewModel {
    override fun template(): String = template

    companion object {
        fun <T> Request.uiViewModel(
            template: String,
            data: T,
            principal: RequestContextLens<OAuthPrincipal>
        ) = UiViewModel(
            template = template,
            data = data,
            principal = principal.extract(this)
        )
    }
}