package org.kotlink.framework.oauth

sealed interface UserPrincipal {
    fun isAnonymous(): Boolean
    val email: String
}

data class OAuthUserPrincipal(
    override val email: String
) : UserPrincipal {
    override fun isAnonymous(): Boolean = false
}

object AnonymousUserPrincipal : UserPrincipal {
    override fun isAnonymous(): Boolean = true
    override val email: String
        get() = throw IllegalStateException("Anonymous user does not have an email address.")
}