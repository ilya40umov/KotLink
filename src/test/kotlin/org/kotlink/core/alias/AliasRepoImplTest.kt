package org.kotlink.core.alias

import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeGreaterThan
import org.amshove.kluent.shouldContain
import org.amshove.kluent.shouldEndWith
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldNotBe
import org.amshove.kluent.shouldStartWith
import org.amshove.kluent.shouldThrow
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.kotlink.ExposedRepoTest
import org.kotlink.core.account.UserAccount
import org.kotlink.core.account.UserAccountRepo
import org.kotlink.core.exposed.DatabaseException
import org.kotlink.core.namespace.Namespace
import org.kotlink.core.namespace.NamespaceRepo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.junit4.SpringRunner
import java.util.UUID

@RunWith(SpringRunner::class)
@ExposedRepoTest
class AliasRepoImplTest {

    lateinit var testUserAccount: UserAccount
    lateinit var testNamespace: Namespace
    lateinit var testAlias: Alias

    @Autowired
    private lateinit var repo: AliasRepo

    @Autowired
    private lateinit var namespaceRepo: NamespaceRepo

    @Autowired
    private lateinit var userAccountRepo: UserAccountRepo

    @Before
    fun setUp() {
        testUserAccount = userAccountRepo.insert(UserAccount(email = "zorro@gmail.com"))
        testNamespace = namespaceRepo.insert(
            Namespace(keyword = UUID.randomUUID().toString(), ownerAccount = testUserAccount))
        testAlias = repo.insert(
            Alias(
                id = 0,
                namespace = testNamespace,
                link = UUID.randomUUID().toString(),
                redirectUrl = UUID.randomUUID().toString(),
                description = "Test alias",
                ownerAccount = testUserAccount
            )
        )
    }

    @Test
    fun `'findAll' should return the test alias`() {
        repo.findAll(0, Int.MAX_VALUE).also {
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
    fun `'findWithAtLeastOneOfTerms' should not return aliases with partial term matches`() {
        testAlias = repo.update(testAlias.copy(description = "term2"))

        repo.findWithAtLeastOneOfTerms(
            listOf("erm2")
        ).also {
            it.size shouldBe 0
        }
    }

    @Test
    fun `'findWithAllOfTermsInFullLink' should return aliases having all given terms in their full link`() {
        testAlias = repo.update(testAlias.copy(link = "${testAlias.link} term87654321"))

        repo.findWithAllOfTermsInFullLink(
            terms = testAlias.link.split(" ").toList(),
            offset = 0,
            limit = 10
        ).also {
            it shouldContain testAlias
        }
    }

    @Test
    fun `'countWithAllOfTermsInFullLink' should return number of aliases having all given terms in their full link`() {
        testAlias = repo.update(testAlias.copy(link = "${testAlias.link} term87654321"))

        repo.countWithAllOfTermsInFullLink(
            terms = testAlias.link.split(" ").toList()
        ).also {
            it shouldEqual 1
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
                description = "",
                ownerAccount = testUserAccount
            )
        ).also {
            it.id shouldBeGreaterThan 0
            repo.findById(it.id)?.id shouldEqual it.id
        }
    }

    @Test
    fun `'insert' should throw DatabaseException if alias with such link already exists`() {
        {
            repo.insert(testAlias)
        } shouldThrow DatabaseException::class
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

    @Test
    fun `'refreshFullLinksInNamespaceWithId' should update full links in a given namespace`() {
        val newKeyword = UUID.randomUUID().toString();
        namespaceRepo.update(testNamespace.copy(keyword = newKeyword))

        repo.refreshFullLinksInNamespaceWithId(testNamespace.id)

        repo.findById(testAlias.id).also {
            it shouldNotBe null
            it?.fullLink shouldNotBe null
            it?.fullLink?.shouldStartWith(newKeyword)
        }
    }
}