package org.kotlink.api.security

import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import java.net.HttpURLConnection.HTTP_UNAUTHORIZED
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class SecretAuthFailureHandler : AuthenticationFailureHandler {

    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException
    ) {
        response.status = HTTP_UNAUTHORIZED
        response.writer.print("""{"error": "Unauthorized"}""")
    }
}

