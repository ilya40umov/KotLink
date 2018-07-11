package org.kotlink.core.alias

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldThrow
import org.junit.Test
import org.junit.runner.RunWith
import org.kotlink.INBOX_ALIAS
import org.kotlink.core.exposed.RecordNotFoundException
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
    fun `'findByFullLinkPrefix' `() {
        // TODO()
    }

    @Test
    fun `'searchAliasesMatchingInput' `() {
        // TODO()
    }

//    @Test
//    fun `'searchAliasesMatchingInput' split keywords in user input and return found aliases`() {
//        whenever(aliasService.findWithAtLeastOneOfKeywords(listOf("inbox", "gmail")))
//            .thenReturn(listOf(INBOX_ALIAS))
//
//        val aliases = kotLinkService.searchAliasesMatchingInput("inbox gmail")
//
//        aliases shouldEqual listOf(INBOX_ALIAS)
//    }

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