package org.kotlink.core.oauth

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.amshove.kluent.shouldContain
import org.amshove.kluent.shouldThrow
import org.junit.Test
import org.junit.runner.RunWith
import org.kotlink.core.account.UserAccountService
import org.mockito.junit.MockitoJUnitRunner
import org.springframework.security.authentication.BadCredentialsException
import javax.servlet.http.HttpSession

@RunWith(MockitoJUnitRunner::class)
class OAuthAuthoritiesExtractorTest {

    private val session = mock<HttpSession> {}
    private val userAccountService = mock<UserAccountService> {}

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
            .also {
                it.map { it.authority } shouldContain "ROLE_USER"
            }
    }

    @Test
    fun `'extractAuthorities' should return ROLE_USER if email is one of the allowed set`() {
        extractor(allowedEmails = setOf("zorro@gmail.com"))
            .extractAuthorities(mutableMapOf("email" to "zorro@gmail.com"))
            .also {
                it.map { it.authority } shouldContain "ROLE_USER"
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