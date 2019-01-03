package org.kotlink.core.secret

import org.amshove.kluent.shouldEqual
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.kotlink.ExposedRepoTest
import org.kotlink.core.account.UserAccount
import org.kotlink.core.account.UserAccountRepo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.*

@ExtendWith(SpringExtension::class)
@ExposedRepoTest
class ApiSecretRepoImplTest(
    @Autowired private val repo: ApiSecretRepo,
    @Autowired private val userAccountRepo: UserAccountRepo
) {

    private val secret = UUID.randomUUID().toString()

    private lateinit var testUserAccount: UserAccount

    @BeforeEach
    internal fun setUp() {
        testUserAccount = userAccountRepo.insert(UserAccount(email = "zorro@gmail.com"))
        repo.insert(ApiSecret(secret = secret, userAccount = testUserAccount))
    }

    @Test
    fun `'findBySecret' should return ApiSecret if provided secret matches a record in database`() {
        repo.findBySecret(secret).also {
            it?.secret shouldEqual secret
        }
    }

    @Test
    fun `'findBySecret' should not return ApiSecret if provided secret does not match a record in database`() {
        repo.findBySecret(UUID.randomUUID().toString()).also {
            it shouldEqual null
        }
    }

    @Test
    fun `'findByUserEmail' should return ApiSecret if provided email matches a record in database`() {
        repo.findByUserEmail(testUserAccount.email).also {
            it?.userAccount shouldEqual testUserAccount
        }
    }

    @Test
    fun `'findByUserEmail' should not return ApiSecret if provided email does not match a record in database`() {
        repo.findByUserEmail(UUID.randomUUID().toString()).also {
            it shouldEqual null
        }
    }

}