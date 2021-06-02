package org.kotlink.core

import com.nhaarman.mockitokotlin2.whenever
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.kotlink.core.account.UserAccountService
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.security.oauth2.provider.OAuth2Request

@ExtendWith(MockitoExtension::class)
class CurrentUserTest(
    @Mock private val userAccountService: UserAccountService,
    @Mock private val storedRequest: OAuth2Request,
    @Mock private val authenticationToken: UsernamePasswordAuthenticationToken
) {
    private val currentUser = CurrentUser(userAccountService)

    @Test
    fun `'getEmail' should return user email if authentication is oauth2`() {
        SecurityContextHolder.getContext().authentication = OAuth2Authentication(
            storedRequest,
            authenticationToken
        )
        whenever(authenticationToken.details).thenReturn(mapOf("email" to "batman@gmail.com"))

        currentUser.getEmail().also {
            it shouldBeEqualTo "batman@gmail.com"
        }
    }

    @Test
    fun `'getEmail' should return unknown as user email if authentication is not available`() {
        SecurityContextHolder.getContext().authentication = null

        currentUser.getEmail().also {
            it shouldBeEqualTo CurrentUser.UNKNOWN_USER_EMAIL
        }
    }
}