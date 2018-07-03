package org.kotlink.ui

import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope

@Component
@RequestScope
class ViewUtils {

    fun truncateRedirectUrl(redirectUrl: String) = when {
        redirectUrl.length > 60 -> redirectUrl.substring(0, 60) + "..."
        else -> redirectUrl
    }
}
