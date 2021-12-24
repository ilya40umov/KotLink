package org.kotlink.framework.ui

import org.http4k.core.Request
import org.http4k.lens.RequestContextLens
import org.http4k.template.ViewModel
import org.kotlink.domain.alias.Alias
import org.kotlink.framework.oauth.OAuthPrincipal

class UiViewModel(
    private val template: String,
    val data: Map<String, *>,
    val principal: OAuthPrincipal?
) : ViewModel {
    override fun template(): String = template

    // TODO move to a separate class
    fun asGoLink(alias: Alias): String {
        val namespace = if (alias.linkPrefix.isBlank()) "" else "<b>${alias.linkPrefix}</b> "
        return "kk/$namespace${alias.link}".replace(' ', '␣')
    }

    // TODO move to a separate class
    fun truncateRedirectUrl(redirectUrl: String) = when {
        redirectUrl.length > DISPLAYED_URL_LENGTH -> redirectUrl.substring(0, DISPLAYED_URL_LENGTH) + "..."
        else -> redirectUrl
    }

    companion object {
        private const val DISPLAYED_URL_LENGTH = 60

        fun Request.uiViewModel(
            template: String,
            data: Map<String, *>,
            principal: RequestContextLens<OAuthPrincipal>
        ) = UiViewModel(
            template = template,
            data = data,
            principal = principal.extract(this)
        )
    }
}