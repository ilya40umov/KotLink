package org.kotlink.core.alias

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.amshove.kluent.shouldContain
import org.amshove.kluent.shouldContainAll
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldThrow
import org.junit.Test
import org.junit.runner.RunWith
import org.kotlink.DEFAULT_NAMESPACE
import org.kotlink.INBOX_ALIAS
import org.kotlink.core.exposed.RecordNotFoundException
import org.kotlink.core.namespace.Namespace
import org.kotlink.core.namespace.NamespaceRepo
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class AliasServiceTest {

    private val aliasRepo = mock<AliasRepo>()
    private val namespaceRepo = mock<NamespaceRepo>()
    private val service = AliasService(aliasRepo, namespaceRepo)

    @Test
    fun `'findByFullLink' should return alias if it can be found in default namespace`() {
        whenever(aliasRepo.findByNamespaceAndLink("", "inbox"))
            .thenReturn(INBOX_ALIAS)

        service.findByFullLink("inbox").also {
            it shouldEqual INBOX_ALIAS
        }
    }

    @Test
    fun `'findByFullLink' should return alias if it can be found in other namespaces`() {
        whenever(aliasRepo.findByNamespaceAndLink("google", "inbox"))
            .thenReturn(INBOX_ALIAS)

        service.findByFullLink("google inbox").also {
            it shouldEqual INBOX_ALIAS
        }
    }

    @Test
    fun `'findByFullLink' should not return any alias if it can't be found by full link`() {
        service.findByFullLink("google inbox").also {
            it shouldEqual null
        }
    }

    @Test
    fun `'findByFullLinkPrefix' should return aliases from both default and the matching namespace if prefix has at least 2 words`() {
        whenever(aliasRepo.findByNamespaceAndLinkPrefix("", "google inb"))
            .thenReturn(listOf(INBOX_ALIAS))
        whenever(aliasRepo.findByNamespaceAndLinkPrefix("google", "inb"))
            .thenReturn(listOf(INBOX_ALIAS.copy(namespace = Namespace(keyword = "google"), link = "inbound")))

        service.findByFullLinkPrefix("google inb").also {
            it.map { it.fullLink } shouldContainAll arrayOf(INBOX_ALIAS.fullLink, "google inbound")
        }
    }

    @Test
    fun `'findByFullLinkPrefix' should return aliases from default and namespaces matching prefix if prefix has only single word`() {
        whenever(aliasRepo.findByNamespaceAndLinkPrefix("", "inb"))
            .thenReturn(listOf(INBOX_ALIAS))
        whenever(aliasRepo.findByNamespacePrefix("inb"))
            .thenReturn(listOf(INBOX_ALIAS.copy(namespace = Namespace(keyword = "inbound"), link = "whatever")))

        service.findByFullLinkPrefix("inb").also {
            it.map { it.fullLink } shouldContainAll arrayOf(INBOX_ALIAS.fullLink, "inbound whatever")
        }
    }

    @Test
    fun `'searchAliasesMatchingInput should split user input and return aliases matching one of keywords if first term is not a namespace' `() {
        whenever(aliasRepo.findWithAtLeastOneOfTerms(listOf("inbox", "gmail")))
            .thenReturn(listOf(INBOX_ALIAS))

        service.searchAliasesMatchingInput("inbox gmail").also {
            it.map { it.fullLink } shouldContain INBOX_ALIAS.fullLink
        }
    }

    @Test
    fun `'searchAliasesMatchingInput should return all aliases in namespace and aso matching one of keywords if the only term is a namespace' `() {
        whenever(namespaceRepo.findByKeyword("google"))
            .thenReturn(DEFAULT_NAMESPACE.copy(keyword = "google"))
        whenever(aliasRepo.findByNamespace("google"))
            .thenReturn(listOf(INBOX_ALIAS.copy(namespace = Namespace(keyword = "google"), link = "tree")))
        whenever(aliasRepo.findWithAtLeastOneOfTerms(listOf("google")))
            .thenReturn(listOf(INBOX_ALIAS))

        service.searchAliasesMatchingInput("google").also {
            it.map { it.fullLink } shouldContainAll listOf(INBOX_ALIAS.fullLink, "google tree")
        }
    }

    @Test
    fun `'create' should throw exception if the new full link is already taken`() {
        whenever(aliasRepo.findByNamespaceAndLink(INBOX_ALIAS.namespace.keyword, INBOX_ALIAS.link))
            .thenReturn(INBOX_ALIAS);

        { service.create(INBOX_ALIAS) } shouldThrow FullLinkExistsException::class
    }

    @Test
    fun `'create' should return the created alias if full link is not yet taken`() {
        whenever(aliasRepo.insert(any()))
            .thenReturn(INBOX_ALIAS)

        service.create(INBOX_ALIAS).also {
            it.fullLink shouldEqual INBOX_ALIAS.fullLink
        }
    }

    @Test
    fun `'update' should throw exception if alias can't be found`() {
        whenever(aliasRepo.findByIdOrThrow(INBOX_ALIAS.id))
            .thenThrow(RecordNotFoundException("Fake exception"));

        { service.update(INBOX_ALIAS) } shouldThrow RecordNotFoundException::class
    }

    @Test
    fun `'update' should throw exception if the new full link is taken`() {
        whenever(aliasRepo.findByIdOrThrow(INBOX_ALIAS.id))
            .thenReturn(INBOX_ALIAS)
        whenever(aliasRepo.findByNamespaceAndLink("", "inbox test"))
            .thenReturn(INBOX_ALIAS.copy(link = "inbox test"));

        { service.update(INBOX_ALIAS.copy(link = "inbox test")) } shouldThrow FullLinkExistsException::class
    }

    @Test
    fun `'update' should return updated alias if provided alias is valid`() {
        whenever(aliasRepo.findByIdOrThrow(INBOX_ALIAS.id))
            .thenReturn(INBOX_ALIAS)
        whenever(aliasRepo.update(any()))
            .thenReturn(INBOX_ALIAS)

        service.update(INBOX_ALIAS).also {
            it.fullLink shouldEqual INBOX_ALIAS.fullLink
        }
    }

    @Test
    fun `'deleteById' should throw exception if alias can't be found`() {
        whenever(aliasRepo.findByIdOrThrow(INBOX_ALIAS.id))
            .thenThrow(RecordNotFoundException("Fake exception"));

        { service.deleteById(INBOX_ALIAS.id) } shouldThrow RecordNotFoundException::class
    }

    @Test
    fun `'deleteById' should return deleted alias if provided alias was deleted with success`() {
        whenever(aliasRepo.findByIdOrThrow(INBOX_ALIAS.id))
            .thenReturn(INBOX_ALIAS)

        service.deleteById(INBOX_ALIAS.id).also {
            it.fullLink shouldEqual INBOX_ALIAS.fullLink
        }
    }
}