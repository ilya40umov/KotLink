package org.kotlink.core.secret

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.amshove.kluent.any
import org.amshove.kluent.shouldEqual
import org.junit.Test
import org.junit.runner.RunWith
import org.kotlink.TEST_SECRET
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class ApiSecretServiceTest {

    private val apiSecretRepo = mock<ApiSecretRepo>()
    private val service = ApiSecretService(apiSecretRepo)

    @Test
    fun `'findBySecret' should return ApiSecret if the provided secret matches a secret in database`() {
        whenever(apiSecretRepo.findBySecret(TEST_SECRET.secret)).thenReturn(TEST_SECRET)

        service.findBySecret(TEST_SECRET.secret).also {
            it shouldEqual TEST_SECRET
        }
    }

    @Test
    fun `'findBySecret' should not return ApiSecret if the provided secret does not match a secret in database`() {
        whenever(apiSecretRepo.findBySecret(TEST_SECRET.secret)).thenReturn(null)

        service.findBySecret(TEST_SECRET.secret).also {
            it shouldEqual null
        }
    }

    @Test
    fun `'findOrCreateForEmail' should return an existing ApiSecret if the provided email matches it`() {
        whenever(apiSecretRepo.findByUserEmail(TEST_SECRET.userEmail)).thenReturn(TEST_SECRET)

        service.findOrCreateForEmail(TEST_SECRET.userEmail).also {
            it shouldEqual TEST_SECRET
            verify(apiSecretRepo, times(0)).insert(any())
        }
    }

    @Test
    fun `'findOrCreateForEmail' should create a new ApiSecret if the provided secret does not match anything`() {
        whenever(apiSecretRepo.findByUserEmail(TEST_SECRET.userEmail)).thenReturn(null)
        whenever(apiSecretRepo.insert(any())).thenReturn(TEST_SECRET)

        service.findOrCreateForEmail(TEST_SECRET.userEmail).also {
            it shouldEqual TEST_SECRET
            verify(apiSecretRepo, times(1)).insert(any())
        }
    }
}