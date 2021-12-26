package org.kotlink.framework.ui

import org.kotlink.domain.alias.Alias

object ViewUtils {
    private const val DISPLAYED_URL_LENGTH = 60

    fun asGoLink(alias: Alias): String {
        val prefix = if (alias.linkPrefix.isBlank()) "" else "<b>${alias.linkPrefix}</b> "
        return "kk/$prefix${alias.link}".replace(' ', '␣')
    }

    fun asPlainGoLink(alias: Alias): String {
        val prefix = if (alias.linkPrefix.isBlank()) "" else "${alias.linkPrefix} "
        return "kk/$prefix${alias.link}".replace(' ', '␣')
    }

    fun truncateRedirectUrl(redirectUrl: String) = when {
        redirectUrl.length > DISPLAYED_URL_LENGTH -> redirectUrl.substring(0,
            DISPLAYED_URL_LENGTH
        ) + "..."
        else -> redirectUrl
    }
}