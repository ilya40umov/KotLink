package org.kotlink.core.security

import org.springframework.boot.autoconfigure.security.SecurityProperties
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority

class ActuatorAuthenticationProvider(
    securityProperties: SecurityProperties
) : AuthenticationProvider {

    private val username = securityProperties.user.name
    private val password = securityProperties.user.password
    private val authorities = securityProperties.user.roles.map { SimpleGrantedAuthority("ROLE_$it") }

    override fun authenticate(authentication: Authentication?): Authentication {
        return when {
            authentication is UsernamePasswordAuthenticationToken &&
                authentication.principal == username && authentication.credentials == password ->
                UsernamePasswordAuthenticationToken(username, password, authorities)
            else -> throw BadCredentialsException("Invalid credentials")
        }
    }

    override fun supports(authentication: Class<*>?): Boolean {
        return authentication != null &&
            UsernamePasswordAuthenticationToken::class.java.isAssignableFrom(authentication)
    }
}