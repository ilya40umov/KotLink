package org.kotlink.api.security

import org.kotlink.core.secret.ApiSecretService
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication

class SecretValidator(private val apiSecretService: ApiSecretService): AuthenticationProvider {

    override fun authenticate(authentication: Authentication): Authentication {
        val secretAuthToken = authentication as SecretAuthToken
        val apiSecret =
            apiSecretService.findBySecret(secretAuthToken.secret)
                ?: throw BadCredentialsException("Provided secret does not exist!")
        return SecretAuthToken(authentication.secret, apiSecret)
    }

    override fun supports(authentication: Class<*>): Boolean =
        SecretAuthToken::class.java.isAssignableFrom(authentication)

}