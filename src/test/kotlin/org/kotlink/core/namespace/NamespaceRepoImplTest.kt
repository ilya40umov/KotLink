package org.kotlink.core.namespace

import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeGreaterThan
import org.amshove.kluent.shouldContain
import org.amshove.kluent.shouldEndWith
import org.amshove.kluent.shouldThrow
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.kotlink.ExposedRepoTest
import org.kotlink.core.account.UserAccount
import org.kotlink.core.account.UserAccountRepo
import org.kotlink.core.exposed.DatabaseException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.UUID

@ExtendWith(SpringExtension::class)
@ExposedRepoTest
class NamespaceRepoImplTest(
    @Autowired private val repo: NamespaceRepo,
    @Autowired private val userAccountRepo: UserAccountRepo
) {

    private val testKeyword = UUID.randomUUID().toString()

    lateinit var testUserAccount: UserAccount

    @BeforeEach
    internal fun setUp() {
        testUserAccount = userAccountRepo.insert(UserAccount(email = "zorro@gmail.com"))
        repo.insert(Namespace(keyword = testKeyword, ownerAccount = testUserAccount))
    }

    @Test
    fun `'findAll' should return test namespace`() {
        repo.findAll().also { namespace ->
            namespace.map { it.keyword } shouldContain testKeyword
        }
    }

    @Test
    fun `'findById' should return namespace if ID is present in database`() {
        val existingId = repo.findAll().find { it.keyword == testKeyword }!!.id

        repo.findById(existingId).also {
            it?.keyword shouldBeEqualTo testKeyword
        }
    }

    @Test
    fun `'findById' should return no namespace if ID is not present in database`() {
        repo.findById(Long.MAX_VALUE).also {
            it shouldBeEqualTo null
        }
    }

    @Test
    fun `'findByKeyword' should return namespace if keyword is present in database`() {
        repo.findByKeyword(testKeyword).also {
            it?.keyword shouldBeEqualTo testKeyword
        }
    }

    @Test
    fun `'findByKeyword' should return no namespace if keyword is not present in database`() {
        repo.findByKeyword(UUID.randomUUID().toString()).also {
            it shouldBeEqualTo null
        }
    }

    @Test
    fun `'insert' should return the namespace with the assigned ID`() {
        repo.insert(
            Namespace(
                keyword = UUID.randomUUID().toString(),
                ownerAccount = testUserAccount
            )
        ).also {
            it.id shouldBeGreaterThan 0
            repo.findById(it.id)?.id shouldBeEqualTo it.id
        }
    }

    @Test
    fun `'insert' should throw DatabaseException if namespace with such keyword already exists`() {
        {
            repo.insert(Namespace(keyword = testKeyword, ownerAccount = testUserAccount))
        } shouldThrow DatabaseException::class
    }

    @Test
    fun `'update' should update the provided namespace`() {
        val existingNamespace = repo.insert(
            Namespace(
                keyword = UUID.randomUUID().toString(),
                ownerAccount = testUserAccount
            )
        )

        repo.update(existingNamespace.copy(keyword = existingNamespace.keyword + ".abc")).also {
            it.id shouldBeEqualTo existingNamespace.id
            repo.findById(it.id)?.keyword!! shouldEndWith ".abc"
        }
    }

    @Test
    fun `'deleteById' should return True if ID existed in database`() {
        val existingId = repo.insert(
            Namespace(
                keyword = UUID.randomUUID().toString(),
                ownerAccount = testUserAccount
            )
        ).id

        repo.deleteById(existingId).also {
            it shouldBeEqualTo true
        }
    }

    @Test
    fun `'deleteById' should return False if ID didn't exist in database`() {
        repo.deleteById(Long.MAX_VALUE).also {
            it shouldBeEqualTo false
        }
    }
}