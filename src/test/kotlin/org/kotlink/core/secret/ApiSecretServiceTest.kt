package org.kotlink.core.secret

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.kotlink.TEST_ACCOUNT
import org.kotlink.TEST_SECRET
import org.kotlink.core.account.UserAccountService
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class ApiSecretServiceTest(
    @Mock private val apiSecretRepo: ApiSecretRepo,
    @Mock private val userAccountService: UserAccountService
) {

    private val service = ApiSecretService(apiSecretRepo, userAccountService)

    @Test
    fun `'findBySecret' should return ApiSecret if the provided secret matches a secret in database`() {
        whenever(apiSecretRepo.findBySecret(TEST_SECRET.secret))
            .thenReturn(TEST_SECRET)

        service.findBySecret(TEST_SECRET.secret).also {
            it shouldBeEqualTo TEST_SECRET
        }
    }

    @Test
    fun `'findBySecret' should not return ApiSecret if the provided secret does not match a secret in database`() {
        whenever(apiSecretRepo.findBySecret(TEST_SECRET.secret))
            .thenReturn(null)

        service.findBySecret(TEST_SECRET.secret).also {
            it shouldBeEqualTo null
        }
    }

    @Test
    fun `'findOrCreateForEmail' should return an existing ApiSecret if the provided email matches it`() {
        whenever(apiSecretRepo.findByUserEmail(TEST_SECRET.userAccount.email))
            .thenReturn(TEST_SECRET)

        service.findOrCreateForEmail(TEST_SECRET.userAccount.email).also {
            it shouldBeEqualTo TEST_SECRET
            verify(apiSecretRepo, times(0)).insert(any())
        }
    }

    @Test
    fun `'findOrCreateForEmail' should create a new ApiSecret if the provided secret does not match anything`() {
        whenever(apiSecretRepo.findByUserEmail(TEST_SECRET.userAccount.email))
            .thenReturn(null)
        whenever(apiSecretRepo.insert(any()))
            .thenReturn(TEST_SECRET)
        whenever(userAccountService.findOrCreateAccountForEmail(any()))
            .thenReturn(TEST_ACCOUNT)

        service.findOrCreateForEmail(TEST_SECRET.userAccount.email).also {
            it shouldBeEqualTo TEST_SECRET
            verify(apiSecretRepo, times(1)).insert(any())
        }
    }
}