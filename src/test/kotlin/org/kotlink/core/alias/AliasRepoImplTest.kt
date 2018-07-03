package org.kotlink.core.alias

import org.amshove.kluent.shouldBeGreaterThan
import org.amshove.kluent.shouldContain
import org.amshove.kluent.shouldEndWith
import org.amshove.kluent.shouldEqual
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.kotlink.ExposedRepoTest
import org.kotlink.core.namespace.Namespace
import org.kotlink.core.namespace.NamespaceRepo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.junit4.SpringRunner
import java.util.UUID

@RunWith(SpringRunner::class)
@ExposedRepoTest
class AliasRepoImplTest {

    lateinit var testNamespace: Namespace
    lateinit var testAlias: Alias

    @Autowired
    private lateinit var repo: AliasRepo

    @Autowired
    private lateinit var namespaceRepo: NamespaceRepo

    @Before
    fun setUp() {
        testNamespace = namespaceRepo.insert(Namespace(keyword = UUID.randomUUID().toString()))
        testAlias = repo.insert(
            Alias(
                id = 0,
                namespace = testNamespace,
                link = UUID.randomUUID().toString(),
                redirectUrl = UUID.randomUUID().toString(),
                description = "Test alias"
            )
        )
    }

    @After
    fun tearDown() {
        repo.findByNamespace(testNamespace.keyword).forEach {
            repo.deleteById(it.id)
        }
        namespaceRepo.deleteById(testNamespace.id)
    }

    @Test
    fun `'findAll' should return the test alias`() {
        repo.findAll().also {
            it.map { it.link } shouldContain testAlias.link
        }
    }

    @Test
    fun `'findById' should return an alias if provided ID matches one`() {
        repo.findById(testAlias.id).also {
            it?.link shouldEqual testAlias.link
        }
    }

    @Test
    fun `'findById' should return no alias if provided ID does not match any of the aliases`() {
        repo.findById(Long.MAX_VALUE).also {
            it shouldEqual null
        }
    }

    @Test
    fun `'findByNamespace' should return aliases from the given namespace`() {
        repo.findByNamespace(testNamespace.keyword).also {
            it shouldContain testAlias
        }
    }

    @Test
    fun `'findByNamespace' should return not aliases if given keyword does not match any namespace`() {
        repo.findByNamespace(UUID.randomUUID().toString()).also {
            it.size shouldEqual 0
        }
    }

    @Test
    fun `'findByNamespacePrefix' should return aliases from namespaces starting with the prefix`() {
        repo.findByNamespacePrefix(testNamespace.keyword.let { it.substring(0, it.length - 5) }).also {
            it shouldContain testAlias
        }
    }

    @Test
    fun `'findByNamespaceAndLink' should return the alias matching the given link and namespace`() {
        repo.findByNamespaceAndLink(testNamespace.keyword, testAlias.link).also {
            it?.link shouldEqual testAlias.link
        }
    }

    @Test
    fun `'findByNamespaceAndLinkPrefix' should return aliases matching the given link and namespace`() {
        repo.findByNamespaceAndLinkPrefix(
            testNamespace.keyword,
            testAlias.link.let { it.substring(0, it.length - 5) }
        ).also {
            it shouldContain testAlias
        }
    }

    @Test
    fun `'findByNamespaceAndWithAtLeastOneOfTerms' should return aliases matching one of terms and namespace`() {
        testAlias = repo.update(testAlias.copy(description = "term1"))

        repo.findByNamespaceAndWithAtLeastOneOfTerms(
            testNamespace.keyword,
            listOf("term1")
        ).also {
            it shouldContain testAlias
        }
    }

    @Test
    fun `'findWithAtLeastOneOfTerms' should return aliases matching one of terms`() {
        testAlias = repo.update(testAlias.copy(description = "term2"))

        repo.findWithAtLeastOneOfTerms(
            listOf("term2")
        ).also {
            it shouldContain testAlias
        }
    }

    @Test
    fun `'insert' should return the inserted alias containing the assigned ID`() {
        repo.insert(
            Alias(
                id = 0,
                namespace = testNamespace,
                link = UUID.randomUUID().toString(),
                redirectUrl = "",
                description = "")
        ).also {
            it.id shouldBeGreaterThan 0
            repo.findById(it.id)?.id shouldEqual it.id
        }
    }

    @Test
    fun `'update' should update the provided alias`() {
        repo.update(
            testAlias.copy(
                link = testAlias.link + "abc",
                redirectUrl = testAlias.redirectUrl + "abc"
            )
        ).also {
            it.id shouldEqual testAlias.id
            repo.findById(it.id)?.link!! shouldEndWith "abc"
            repo.findById(it.id)?.redirectUrl!! shouldEndWith "abc"
        }
    }

    @Test
    fun `'deleteById' should return True if alias was found and deleted`() {
        repo.deleteById(testAlias.id).also {
            it shouldEqual true
        }
    }

    @Test
    fun `'deleteById' should return False if alias was not found`() {
        repo.deleteById(Long.MAX_VALUE).also {
            it shouldEqual false
        }
    }
}