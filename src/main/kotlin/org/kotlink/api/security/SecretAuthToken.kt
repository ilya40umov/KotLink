package org.kotlink.api.security

import org.kotlink.core.secret.ApiSecret
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.authority.AuthorityUtils

class SecretAuthToken(
    val secret: String,
    val apiSecret: ApiSecret? = null
) : AbstractAuthenticationToken(AuthorityUtils.NO_AUTHORITIES) {

    init {
        isAuthenticated = apiSecret != null
    }

    override fun getCredentials(): Any = secret

    override fun getPrincipal(): Any? = apiSecret
}
