package org.kotlink.ui

import org.kotlink.core.alias.Alias
import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope
import javax.servlet.http.HttpServletRequest

@Component
@RequestScope
class ViewUtils {

    fun truncateRedirectUrl(redirectUrl: String) = when {
        redirectUrl.length > DISPLAYED_URL_LENGTH -> redirectUrl.substring(0, DISPLAYED_URL_LENGTH) + "..."
        else -> redirectUrl
    }

    fun asGoLink(alias: Alias): String {
        val namespace = if (alias.namespace.keyword.isBlank()) "" else "<b>${alias.namespace.keyword}</b> "
        return "kk/$namespace${alias.link}".replace(' ', '␣')
    }

    fun serverUrlFromRequest(request: HttpServletRequest): String {
        val serverPort = when (request.serverPort) {
            in DEFAULT_HTTP_PORTS -> ""
            else -> ":${request.serverPort}"
        }
        return "${request.scheme}://${request.serverName}$serverPort/"
    }

    companion object {
        private val DEFAULT_HTTP_PORTS = setOf(80, 443)
        private const val DISPLAYED_URL_LENGTH = 60
    }
}
