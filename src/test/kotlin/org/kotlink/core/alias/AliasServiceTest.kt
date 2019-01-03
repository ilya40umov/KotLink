package org.kotlink.core.alias

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.whenever
import org.amshove.kluent.shouldContain
import org.amshove.kluent.shouldContainAll
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldThrow
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.kotlink.DEFAULT_NAMESPACE
import org.kotlink.INBOX_ALIAS
import org.kotlink.TEST_ACCOUNT
import org.kotlink.core.CurrentUser
import org.kotlink.core.OperationDeniedException
import org.kotlink.core.exposed.RecordNotFoundException
import org.kotlink.core.namespace.Namespace
import org.kotlink.core.namespace.NamespaceRepo
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class AliasServiceTest(
    @Mock private val aliasRepo: AliasRepo,
    @Mock private val namespaceRepo: NamespaceRepo,
    @Mock private val currentUser: CurrentUser
) {

    private val service = AliasService(aliasRepo, namespaceRepo, currentUser)

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
    @DisplayName(
        """
        'findByFullLinkPrefix' should
        return aliases from both default and the matching namespace
        if prefix has at least 2 words
    """
    )
    fun `'findByFullLinkPrefix' should search in default & matching namespace if prefix is 2+ words`() {
        whenever(aliasRepo.findByNamespaceAndLinkPrefix("", "google inb"))
            .thenReturn(listOf(INBOX_ALIAS))
        whenever(aliasRepo.findByNamespaceAndLinkPrefix("google", "inb"))
            .thenReturn(
                listOf(
                    INBOX_ALIAS.copy(
                        namespace = Namespace(keyword = "google", ownerAccount = TEST_ACCOUNT),
                        link = "inbound"
                    )
                )
            )

        service.findByFullLinkPrefix("google inb").also { aliases ->
            aliases.map { it.fullLink } shouldContainAll arrayOf(INBOX_ALIAS.fullLink, "google inbound")
        }
    }

    @Test
    @DisplayName(
        """
        'findByFullLinkPrefix' should
        return aliases from default and namespaces matching prefix
        if prefix has only single word
    """
    )
    fun `'findByFullLinkPrefix' should search in default & namespace matching prefix if prefix has 1 word`() {
        whenever(aliasRepo.findByNamespaceAndLinkPrefix("", "inb"))
            .thenReturn(listOf(INBOX_ALIAS))
        whenever(aliasRepo.findByNamespacePrefix("inb"))
            .thenReturn(
                listOf(
                    INBOX_ALIAS.copy(
                        namespace = Namespace(keyword = "inbound", ownerAccount = TEST_ACCOUNT),
                        link = "whatever"
                    )
                )
            )

        service.findByFullLinkPrefix("inb").also { aliases ->
            aliases.map { it.fullLink } shouldContainAll arrayOf(INBOX_ALIAS.fullLink, "inbound whatever")
        }
    }

    @Test
    @DisplayName(
        """
        'searchAliasesMatchingAtLeastPartOfInput' should
        split user input and return aliases matching one of keywords
        if first term is not a namespace
    """
    )
    fun `'searchAliasesMatchingAtLeastPartOfInput' should return matches for one of keywords if 1st term not a ns`() {
        whenever(aliasRepo.findWithAtLeastOneOfTerms(listOf("inbox", "gmail")))
            .thenReturn(listOf(INBOX_ALIAS))

        service.searchAliasesMatchingAtLeastPartOfInput("inbox gmail").also { aliases ->
            aliases.map { it.fullLink } shouldContain INBOX_ALIAS.fullLink
        }
    }

    @Test
    @DisplayName(
        """
        'searchAliasesMatchingAtLeastPartOfInput' should
        return all aliases in namespace and also matching one of keywords
        if the only term is a namespace
    """
    )
    fun `'searchAliasesMatchingAtLeastPartOfInput' should return all in ns & matches for keyword if term is a ns`() {
        whenever(namespaceRepo.findByKeyword("google"))
            .thenReturn(DEFAULT_NAMESPACE.copy(keyword = "google"))
        whenever(aliasRepo.findByNamespace("google"))
            .thenReturn(
                listOf(
                    INBOX_ALIAS.copy(
                        namespace = Namespace(keyword = "google", ownerAccount = TEST_ACCOUNT),
                        link = "tree"
                    )
                )
            )
        whenever(aliasRepo.findWithAtLeastOneOfTerms(listOf("google")))
            .thenReturn(listOf(INBOX_ALIAS))

        service.searchAliasesMatchingAtLeastPartOfInput("google").also { aliases ->
            aliases.map { it.fullLink } shouldContainAll listOf(INBOX_ALIAS.fullLink, "google tree")
        }
    }

    @Test
    fun `'findAliasesWithFullLinkMatchingEntireInput' should return all aliases if input is empty`() {
        whenever(aliasRepo.findAll(any(), any()))
            .thenReturn(listOf(INBOX_ALIAS))
        whenever(aliasRepo.countAll())
            .thenReturn(1)

        service.findAliasesWithFullLinkMatchingEntireInput(
            userProvidedInput = "  ",
            offset = 0,
            limit = 10
        ).also { page ->
            page.records shouldContain INBOX_ALIAS
            page.offset shouldEqual 0
            page.limit shouldEqual 10
            page.totalCount shouldEqual 1
        }
    }

    @Test
    fun `'findAliasesWithFullLinkMatchingEntireInput' should return only aliases matching terms if input is not empty`() {
        whenever(aliasRepo.findWithAllOfTermsInFullLink(eq(listOf("google")), any(), any()))
            .thenReturn(emptyList())
        whenever(aliasRepo.countWithAllOfTermsInFullLink(eq(listOf("google"))))
            .thenReturn(0)

        service.findAliasesWithFullLinkMatchingEntireInput(
            userProvidedInput = "google ",
            offset = 0,
            limit = 25
        ).also { page ->
            page.records.size shouldEqual 0
            page.offset shouldEqual 0
            page.limit shouldEqual 25
            page.totalCount shouldEqual 0
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
    fun `'update' should throw exception if the current user is not allowed to edit it`() {
        whenever(aliasRepo.findByIdOrThrow(INBOX_ALIAS.id))
            .thenReturn(INBOX_ALIAS)
        whenever(currentUser.getAccount())
            .thenReturn(TEST_ACCOUNT.copy(id = 987));

        { service.update(INBOX_ALIAS.copy(link = "inbox test")) } shouldThrow OperationDeniedException::class
    }

    @Test
    fun `'update' should return updated alias if provided alias is valid`() {
        whenever(aliasRepo.findByIdOrThrow(INBOX_ALIAS.id))
            .thenReturn(INBOX_ALIAS)
        whenever(aliasRepo.update(any()))
            .thenReturn(INBOX_ALIAS)
        whenever(currentUser.getAccount())
            .thenReturn(TEST_ACCOUNT)

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
    fun `'deleteById' should throw exception if the current user is not allowed to edit it`() {
        whenever(aliasRepo.findByIdOrThrow(INBOX_ALIAS.id))
            .thenReturn(INBOX_ALIAS)
        whenever(currentUser.getAccount())
            .thenReturn(TEST_ACCOUNT.copy(id = 987));

        { service.deleteById(INBOX_ALIAS.id) } shouldThrow OperationDeniedException::class
    }

    @Test
    fun `'deleteById' should return deleted alias if provided alias was deleted with success`() {
        whenever(aliasRepo.findByIdOrThrow(INBOX_ALIAS.id))
            .thenReturn(INBOX_ALIAS)
        whenever(currentUser.getAccount())
            .thenReturn(TEST_ACCOUNT)

        service.deleteById(INBOX_ALIAS.id).also {
            it.fullLink shouldEqual INBOX_ALIAS.fullLink
        }
    }
}