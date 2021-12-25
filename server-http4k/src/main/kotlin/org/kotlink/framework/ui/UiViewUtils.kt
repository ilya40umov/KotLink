package org.kotlink.framework.ui

import org.kotlink.domain.alias.Alias

object UiViewUtils {
    private const val DISPLAYED_URL_LENGTH = 60

    fun asGoLink(alias: Alias): String {
        val namespace = if (alias.linkPrefix.isBlank()) "" else "<b>${alias.linkPrefix}</b> "
        return "kk/$namespace${alias.link}".replace(' ', '␣')
    }

    fun truncateRedirectUrl(redirectUrl: String) = when {
        redirectUrl.length > DISPLAYED_URL_LENGTH -> redirectUrl.substring(0,
            DISPLAYED_URL_LENGTH
        ) + "..."
        else -> redirectUrl
    }
}