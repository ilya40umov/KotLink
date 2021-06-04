package org.kotlink.core.oauth

import org.kotlink.core.account.UserAccountService
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority
import javax.servlet.http.HttpSession

open class OAuthAuthoritiesMapper(
    private val session: HttpSession,
    private val allowedEmails: Set<String>,
    private val allowedEmailRegex: Regex,
    private val userAccountService: UserAccountService
) : GrantedAuthoritiesMapper {
    override fun mapAuthorities(
        authorities: MutableCollection<out GrantedAuthority>?
    ): MutableCollection<out GrantedAuthority> {
        val userAuthority = authorities?.find { it is OAuth2UserAuthority } as OAuth2UserAuthority?
        if (userAuthority == null) {
            session.invalidate()
            throw BadCredentialsException("You don't have access to this KotLink server!")
        }
        val email = userAuthority.attributes["email"].toString()
        if (email.isNotBlank() && (allowedEmailRegex.matches(email) || allowedEmails.contains(email))) {
            userAccountService.findOrCreateAccountForEmail(email)
            return AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_USER")
        } else {
            session.invalidate()
            throw BadCredentialsException("You don't have access to this KotLink server!")
        }
    }
}