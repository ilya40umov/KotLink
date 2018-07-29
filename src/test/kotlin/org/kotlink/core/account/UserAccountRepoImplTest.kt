package org.kotlink.core.account

import org.amshove.kluent.shouldBeGreaterThan
import org.amshove.kluent.shouldEqual
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.kotlink.ExposedRepoTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.junit4.SpringRunner
import java.util.UUID

@RunWith(SpringRunner::class)
@ExposedRepoTest
class UserAccountRepoImplTest {

    lateinit var testUserAccount: UserAccount

    @Autowired
    private lateinit var repo: UserAccountRepo

    @Before
    fun setUp() {
        testUserAccount = repo.insert(UserAccount(email = "zorro@gmail.com"))
    }

    @Test
    fun `'findById' should return the user account if provided ID matches one`() {
        repo.findById(testUserAccount.id).also {
            it?.email shouldEqual testUserAccount.email
        }
    }

    @Test
    fun `'findById' should return no user account if provided ID does not match any of the user accounts`() {
        repo.findById(Long.MAX_VALUE).also {
            it shouldEqual null
        }
    }

    @Test
    fun `'findByUserEmail' should return the user account if provided email matches one`() {
        repo.findByUserEmail(testUserAccount.email).also {
            it?.id shouldEqual testUserAccount.id
        }
    }

    @Test
    fun `'findByUserEmail' should return no user account if provided email does not match any of the user accounts`() {
        repo.findByUserEmail(UUID.randomUUID().toString()).also {
            it shouldEqual null
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
            repo.findById(it.id)?.id shouldEqual it.id
        }
    }
}