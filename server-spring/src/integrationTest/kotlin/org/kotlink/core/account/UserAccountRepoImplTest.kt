package org.kotlink.core.account

import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeGreaterThan
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.kotlink.ExposedRepoTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.UUID

@ExtendWith(SpringExtension::class)
@ExposedRepoTest
class UserAccountRepoImplTest(
    @Autowired private val repo: UserAccountRepo
) {

    @Test
    fun `'findById' should return the user account if provided ID matches one`() {
        val testUserAccount = repo.insert(UserAccount(email = "zorro@gmail.com"))

        repo.findById(testUserAccount.id).also {
            it?.email shouldBeEqualTo testUserAccount.email
        }
    }

    @Test
    fun `'findById' should return no user account if provided ID does not match any of the user accounts`() {
        repo.findById(Long.MAX_VALUE).also {
            it shouldBeEqualTo null
        }
    }

    @Test
    fun `'findByUserEmail' should return the user account if provided email matches one`() {
        val testUserAccount = repo.insert(UserAccount(email = "zorro@gmail.com"))

        repo.findByUserEmail(testUserAccount.email).also {
            it?.id shouldBeEqualTo testUserAccount.id
        }
    }

    @Test
    fun `'findByUserEmail' should return no user account if provided email does not match any of the user accounts`() {
        repo.findByUserEmail(UUID.randomUUID().toString()).also {
            it shouldBeEqualTo null
        }
    }

    @Test
    fun `'insert' should return the inserted user account containing the assigned ID`() {
        repo.insert(
            UserAccount(
                id = 0,
                email = "zorro123@gmail.com"
            )
        ).also {
            it.id shouldBeGreaterThan 0
            repo.findById(it.id)?.id shouldBeEqualTo it.id
        }
    }
}