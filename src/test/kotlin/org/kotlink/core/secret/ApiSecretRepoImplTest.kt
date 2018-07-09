package org.kotlink.core.secret

import mu.KLogging
import org.amshove.kluent.shouldEqual
import org.jetbrains.exposed.sql.deleteWhere
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.kotlink.ExposedRepoTest
import org.kotlink.core.exposed.DatabaseException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.junit4.SpringRunner
import java.util.UUID

@RunWith(SpringRunner::class)
@ExposedRepoTest
class ApiSecretRepoImplTest {

    private val secret = UUID.randomUUID().toString()
    private val userEmail = "test@gmail.com"

    @Autowired
    private lateinit var repo: ApiSecretRepo

    @Before
    fun setUp() {
        repo.insert(ApiSecret(secret = secret, userEmail = userEmail))
    }

    @After
    fun tearDown() {
        try {
            ApiSecrets.deleteWhere { ApiSecrets.secret.eq(secret) }
        } catch (e: DatabaseException) {
            logger.warn { "Caught on tearDown: ${e.message}" }
        }
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
        repo.findByUserEmail(userEmail).also {
            it?.userEmail shouldEqual userEmail
        }
    }

    @Test
    fun `'findByUserEmail' should not return ApiSecret if provided email does not match a record in database`() {
        repo.findByUserEmail(UUID.randomUUID().toString()).also {
            it shouldEqual null
        }
    }

    companion object : KLogging()
}