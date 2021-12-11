package org.kotlink.framework.oauth

import com.auth0.jwk.JwkProvider
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import org.http4k.security.openid.IdToken
import java.security.interfaces.RSAPublicKey

class IdTokenProcessor(
    private val jwkProvider: JwkProvider
) {
    fun extractEmail(idToken: IdToken): String {
        val jwt = JWT.decode(idToken.value)
        val jwk = jwkProvider[jwt.keyId]
        Algorithm.RSA256(jwk.publicKey as RSAPublicKey, null).verify(jwt)
        return jwt.claims["email"]?.asString() ?: throw IllegalArgumentException("IdToken is missing email claim.")
    }
}