package org.kotlink.core.oauth

import com.nhaarman.mockitokotlin2.verify
import org.amshove.kluent.shouldContain
import org.amshove.kluent.shouldThrow
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.kotlink.core.account.UserAccountService
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority
import javax.servlet.http.HttpSession

@ExtendWith(MockitoExtension::class)
class GrantedAuthoritiesMapperTest(
    @Mock private val session: HttpSession,
    @Mock private val userAccountService: UserAccountService
) {
    @Test
    fun `'extractAuthorities' should throw BadCredentialsException if user email is blank`() {
        {
            createMapper(allowedEmailRegex = """.*@gmail\.com""".toRegex())
                .mapAuthorities(mutableListOf(OAuth2UserAuthority(mapOf("email" to ""))))
        } shouldThrow BadCredentialsException::class
    }

    @Test
    fun `'extractAuthorities' should return ROLE_USER if email matches the allowed regex`() {
        createMapper(allowedEmailRegex = """.*@gmail\.com""".toRegex())
            .mapAuthorities(mutableListOf(OAuth2UserAuthority(mapOf("email" to "zorro@gmail.com"))))
            .also { authorities ->
                authorities.map { it.authority } shouldContain "ROLE_USER"
            }
    }

    @Test
    fun `'extractAuthorities' should return ROLE_USER if email is one of the allowed set`() {
        createMapper(allowedEmails = setOf("zorro@gmail.com"))
            .mapAuthorities(mutableListOf(OAuth2UserAuthority(mapOf("email" to "zorro@gmail.com"))))
            .also { authorities ->
                authorities.map { it.authority } shouldContain "ROLE_USER"
            }
    }

    @Test
    fun `'extractAuthorities' should throw BadCredentialsException if email is not allowed`() {
        {
            createMapper(allowedEmailRegex = """.*@gmail\.com""".toRegex())
                .mapAuthorities(mutableListOf(OAuth2UserAuthority(mapOf("email" to "zorro@yahoo.com"))))
        } shouldThrow BadCredentialsException::class
    }

    @Test
    fun `'extractAuthorities' should create a user account if email is valid and no account exists for it`() {
        createMapper(allowedEmails = setOf("zorro@gmail.com"))
            .mapAuthorities(mutableListOf(OAuth2UserAuthority(mapOf("email" to "zorro@gmail.com"))))
            .also {
                verify(userAccountService).findOrCreateAccountForEmail("zorro@gmail.com")
            }
    }

    private fun createMapper(allowedEmails: Set<String> = emptySet(), allowedEmailRegex: Regex = "^$".toRegex()) =
        OAuthAuthoritiesMapper(
            session = session,
            allowedEmails = allowedEmails,
            allowedEmailRegex = allowedEmailRegex,
            userAccountService = userAccountService
        )
}
