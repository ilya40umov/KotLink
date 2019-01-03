package org.kotlink.core.oauth

import org.kotlink.core.account.UserAccountService
import org.springframework.boot.autoconfigure.security.oauth2.resource.AuthoritiesExtractor
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.AuthorityUtils
import javax.servlet.http.HttpSession

open class OAuthAuthoritiesExtractor(
    private val session: HttpSession,
    private val allowedEmails: Set<String>,
    private val allowedEmailRegex: Regex,
    private val userAccountService: UserAccountService
) : AuthoritiesExtractor {
    override fun extractAuthorities(map: MutableMap<String, Any>): MutableList<GrantedAuthority> {
        val email = map["email"].toString()
        if (email.isNotBlank() &&
            (allowedEmailRegex.matches(email) || allowedEmails.contains(email))
        ) {
            userAccountService.findOrCreateAccountForEmail(email)
            return AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_USER")
        } else {
            session.invalidate()
            throw BadCredentialsException("You don't have access to this KotLink server!")
        }
    }
}