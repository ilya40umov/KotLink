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
import javax.servlet.http.HttpSession

@ExtendWith(MockitoExtension::class)
class OAuthAuthoritiesExtractorTest(
    @Mock private val session: HttpSession,
    @Mock private val userAccountService: UserAccountService
) {

    private fun extractor(allowedEmails: Set<String> = emptySet(), allowedEmailRegex: Regex = "^$".toRegex()) =
        OAuthAuthoritiesExtractor(
            session = session,
            allowedEmails = allowedEmails,
            allowedEmailRegex = allowedEmailRegex,
            userAccountService = userAccountService
        )

    @Test
    fun `'extractAuthorities' should throw BadCredentialsException if user email is blank`() {
        {
            extractor(allowedEmailRegex = """.*@gmail\.com""".toRegex())
                .extractAuthorities(mutableMapOf("email" to ""))
        } shouldThrow BadCredentialsException::class
    }

    @Test
    fun `'extractAuthorities' should return ROLE_USER if email matches the allowed regex`() {
        extractor(allowedEmailRegex = """.*@gmail\.com""".toRegex())
            .extractAuthorities(mutableMapOf("email" to "zorro@gmail.com"))
            .also { authorities ->
                authorities.map { it.authority } shouldContain "ROLE_USER"
            }
    }

    @Test
    fun `'extractAuthorities' should return ROLE_USER if email is one of the allowed set`() {
        extractor(allowedEmails = setOf("zorro@gmail.com"))
            .extractAuthorities(mutableMapOf("email" to "zorro@gmail.com"))
            .also { authorities ->
                authorities.map { it.authority } shouldContain "ROLE_USER"
            }
    }

    @Test
    fun `'extractAuthorities' should throw BadCredentialsException if email is not allowed`() {
        {
            extractor(allowedEmailRegex = """.*@gmail\.com""".toRegex())
                .extractAuthorities(mutableMapOf("email" to "zorro@yahoo.com"))
        } shouldThrow BadCredentialsException::class
    }

    @Test
    fun `'extractAuthorities' should create a user account if email is valid and no account exists for it`() {
        extractor(allowedEmails = setOf("zorro@gmail.com"))
            .extractAuthorities(mutableMapOf("email" to "zorro@gmail.com"))
            .also {
                verify(userAccountService).findOrCreateAccountForEmail("zorro@gmail.com")
            }
    }
}