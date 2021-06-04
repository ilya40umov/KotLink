package org.kotlink.core

import com.nhaarman.mockitokotlin2.whenever
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.kotlink.core.account.UserAccountService
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.user.OAuth2User

@ExtendWith(MockitoExtension::class)
class CurrentUserTest(
    @Mock private val userAccountService: UserAccountService,
    @Mock private val principal: OAuth2User
) {
    private val currentUser = CurrentUser(userAccountService)

    @Test
    fun `'getEmail' should return user email if authentication is oauth2`() {
        SecurityContextHolder.getContext().authentication =
            OAuth2AuthenticationToken(principal, listOf<GrantedAuthority>(), "google")
        whenever(principal.attributes).thenReturn(mapOf("email" to "batman@gmail.com"))

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