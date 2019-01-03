package org.kotlink.core.account

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.amshove.kluent.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.kotlink.TEST_ACCOUNT
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class UserAccountServiceTest(
    @Mock private val userAccountRepo: UserAccountRepo
) {

    private val service = UserAccountService(userAccountRepo)

    @Test
    fun `'findOrCreateAccountForEmail' should return the existing account if there is an account matching the email`() {
        whenever(userAccountRepo.findByUserEmail(any()))
            .thenReturn(TEST_ACCOUNT)

        service.findOrCreateAccountForEmail(TEST_ACCOUNT.email).also {
            it shouldBe TEST_ACCOUNT
        }
    }

    @Test
    fun `'findOrCreateAccountForEmail' should create a new account if there is no account matching the email`() {
        whenever(userAccountRepo.insert(any()))
            .thenReturn(TEST_ACCOUNT)

        service.findOrCreateAccountForEmail(TEST_ACCOUNT.email).also {
            it shouldBe TEST_ACCOUNT
            verify(userAccountRepo).findByUserEmail(TEST_ACCOUNT.email)
            verify(userAccountRepo).insert(any())
        }
    }
}