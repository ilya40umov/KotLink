package org.kotlink.ui

import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope

@Component
@RequestScope
class ViewUtils {

    fun truncateRedirectUrl(redirectUrl: String) = when {
        redirectUrl.length > DISPLAYED_URL_LENGTH -> redirectUrl.substring(0, DISPLAYED_URL_LENGTH) + "..."
        else -> redirectUrl
    }

    companion object {
        private const val DISPLAYED_URL_LENGTH = 60
    }
}
