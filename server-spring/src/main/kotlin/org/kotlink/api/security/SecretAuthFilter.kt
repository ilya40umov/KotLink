package org.kotlink.api.security

import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter
import org.springframework.security.web.util.matcher.RequestMatcher
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class SecretAuthFilter(
    authPathRequestMatcher: RequestMatcher,
    authenticationManager: AuthenticationManager
) : AbstractAuthenticationProcessingFilter(authPathRequestMatcher) {

    init {
        this.authenticationManager = authenticationManager
        this.setAuthenticationFailureHandler(SecretAuthFailureHandler())
        this.setAuthenticationSuccessHandler { _, _, _ -> /* do nothing */ }
        this.setSessionAuthenticationStrategy { _, _, _ -> /* do nothing */ }
    }

    override fun attemptAuthentication(
        request: HttpServletRequest,
        response: HttpServletResponse
    ): Authentication {
        val authHeader = request.getHeader(HttpHeaders.AUTHORIZATION)
        val secretParam = request.getParameter("secret")
        val secret = when {
            secretParam != null -> secretParam
            authHeader != null && authHeader.startsWith("Bearer ") -> authHeader.substring("Bearer ".length)
            else -> null
        } ?: throw BadCredentialsException("Secret not found in the request")
        return authenticationManager.authenticate(SecretAuthToken(secret))
    }

    override fun successfulAuthentication(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain,
        authResult: Authentication
    ) {
        super.successfulAuthentication(request, response, chain, authResult)
        chain.doFilter(request, response)
    }
}
