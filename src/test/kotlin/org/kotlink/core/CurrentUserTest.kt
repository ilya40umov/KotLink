package org.kotlink.core

import com.nhaarman.mockitokotlin2.whenever
import org.amshove.kluent.shouldEqual
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.kotlink.core.account.UserAccountService
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.security.oauth2.provider.OAuth2Request

@RunWith(MockitoJUnitRunner::class)
class CurrentUserTest {

    @Mock
    private lateinit var userAccountService: UserAccountService

    @Mock
    private lateinit var storedRequest: OAuth2Request

    @Mock
    private lateinit var authenticationToken: UsernamePasswordAuthenticationToken

    private lateinit var currentUser: CurrentUser

    @Before
    fun setUp() {
        currentUser = CurrentUser(userAccountService)
    }

    @Test
    fun `'getEmail' should return user email if authentication is oauth2`() {
        SecurityContextHolder.getContext().authentication = OAuth2Authentication(
            storedRequest,
            authenticationToken
        )
        whenever(authenticationToken.details).thenReturn(mapOf("email" to "batman@gmail.com"))

        currentUser.getEmail().also {
            it shouldEqual "batman@gmail.com"
        }
    }

    @Test
    fun `'getEmail' should return unknown as user email if authentication is not available`() {
        SecurityContextHolder.getContext().authentication = null

        currentUser.getEmail().also {
            it shouldEqual CurrentUser.UNKNOWN_USER_EMAIL
        }
    }
}