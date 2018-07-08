package org.kotlink.core.namespace

import mu.KLogging
import org.amshove.kluent.shouldBeGreaterThan
import org.amshove.kluent.shouldContain
import org.amshove.kluent.shouldEndWith
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldThrow
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
class NamespaceRepoImplTest {

    val testKeyword = UUID.randomUUID().toString()

    @Autowired
    private lateinit var repo: NamespaceRepo

    @Before
    fun setUp() {
        repo.insert(Namespace(keyword = testKeyword))
    }

    @After
    fun tearDown() {
        try {
            repo.findByKeyword(testKeyword)?.id?.also {
                repo.deleteById(it)
            }
        } catch (e: DatabaseException) {
            logger.warn { "Caught on tearDown: ${e.message}" }
        }
    }

    @Test
    fun `'findAll' should return test namespace`() {
        repo.findAll().also {
            it.map { it.keyword } shouldContain testKeyword
        }
    }

    @Test
    fun `'findById' should return namespace if ID is present in database`() {
        val existingId = repo.findAll().find { it.keyword == testKeyword }!!.id

        repo.findById(existingId).also {
            it?.keyword shouldEqual testKeyword
        }
    }

    @Test
    fun `'findById' should return no namespace if ID is not present in database`() {
        repo.findById(Long.MAX_VALUE).also {
            it shouldEqual null
        }
    }

    @Test
    fun `'findByKeyword' should return namespace if keyword is present in database`() {
        repo.findByKeyword(testKeyword).also {
            it?.keyword shouldEqual testKeyword
        }
    }

    @Test
    fun `'findByKeyword' should return no namespace if keyword is not present in database`() {
        repo.findByKeyword(UUID.randomUUID().toString()).also {
            it shouldEqual null
        }
    }

    @Test
    fun `'insert' should return the namespace with the assigned ID`() {
        repo.insert(Namespace(keyword = UUID.randomUUID().toString())).also {
            it.id shouldBeGreaterThan 0
            repo.findById(it.id)?.id shouldEqual it.id
        }
    }

    @Test
    fun `'insert' should throw DatabaseException if namespace with such keyword already exists`() {
        val funx = {repo.insert(Namespace(keyword = testKeyword))}
        funx shouldThrow DatabaseException::class
    }

    @Test
    fun `'update' should update the provided namespace`() {
        val existingNamespace = repo.insert(Namespace(keyword = UUID.randomUUID().toString()))

        repo.update(existingNamespace.copy(keyword = existingNamespace.keyword + ".abc")).also {
            it.id shouldEqual existingNamespace.id
            repo.findById(it.id)?.keyword!! shouldEndWith ".abc"
        }
    }

    @Test
    fun `'deleteById' should return True if ID existed in database`() {
        val existingId = repo.insert(Namespace(keyword = UUID.randomUUID().toString())).id

        repo.deleteById(existingId).also {
            it shouldEqual true
        }
    }

    @Test
    fun `'deleteById' should return False if ID didn't exist in database`() {
        repo.deleteById(Long.MAX_VALUE).also {
            it shouldEqual false
        }
    }

    companion object : KLogging()
}