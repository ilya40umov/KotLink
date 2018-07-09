package org.kotlink.ui

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope

@RequestScope
@Component
class CurrentUser {

    fun getEmail(): String? {
        val auth: Authentication? = SecurityContextHolder.getContext().authentication
        if (auth is OAuth2Authentication) {
            val token = auth.userAuthentication as UsernamePasswordAuthenticationToken
            val details = token.details as Map<*, *>
            return details["email"] as String
        }
        return auth?.name ?: "unknown@user"
    }
}
