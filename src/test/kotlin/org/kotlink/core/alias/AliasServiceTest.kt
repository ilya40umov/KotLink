package org.kotlink.core.alias

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.whenever
import org.amshove.kluent.shouldBe
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
    fun `'findByFullLink' should return alias if it can be found by full link`() {
        whenever(aliasRepo.findByFullLink("inbox"))
            .thenReturn(INBOX_ALIAS)

        service.findByFullLink("inbox").also {
            it shouldEqual INBOX_ALIAS
        }
    }

    @Test
    fun `'findByFullLink' should return alias if it can be found with terms in different order`() {
        whenever(
            aliasRepo.findWithAllOfTermsInFullLink(
                terms = argThat { containsAll(listOf("inbox", "google")) },
                lastTermIsPrefix = eq(false),
                offset = eq(0),
                limit = eq(2)
            )
        ).thenReturn(listOf(INBOX_ALIAS.copy(link = "inbox google")))

        service.findByFullLink("google inbox").also {
            it shouldEqual INBOX_ALIAS.copy(link = "inbox google")
        }
    }

    @Test
    fun `'findByFullLink' should not return any alias if all given terms match but there are extra terms`() {
        whenever(
            aliasRepo.findWithAllOfTermsInFullLink(
                terms = argThat { containsAll(listOf("inbox", "bla")) },
                lastTermIsPrefix = eq(false),
                offset = eq(0),
                limit = eq(2)
            )
        ).thenReturn(listOf(INBOX_ALIAS.copy(link = "inbox bla")))

        service.findByFullLink("google inbox").also {
            it shouldBe null
        }
    }

    @Test
    fun `'findByFullLink' should not return any alias if nothing matching given term can be found`() {
        service.findByFullLink("google inbox").also {
            it shouldEqual null
        }
    }

    @Test
    fun `'findByFullLinkPrefix' should return no aliases if input contains zero terms`() {
        service.findByFullLinkPrefix(" ").also { aliases ->
            aliases.size shouldBe 0
        }
    }

    @Test
    fun `'findByFullLinkPrefix' should return aliases ordered by length of common prefix with input`() {
        whenever(
            aliasRepo.findWithAllOfTermsInFullLink(
                terms = argThat { containsAll(listOf("in")) },
                lastTermIsPrefix = eq(true),
                offset = eq(0),
                limit = eq(Int.MAX_VALUE)
            )
        ).thenReturn(
            listOf(
                INBOX_ALIAS.copy(
                    namespace = Namespace(keyword = "google", ownerAccount = TEST_ACCOUNT),
                    link = "inbox"
                ),
                INBOX_ALIAS
            )
        )

        service.findByFullLinkPrefix("in").also { aliases ->
            aliases.map { it.fullLink } shouldContainAll arrayOf(INBOX_ALIAS.fullLink, "google inbox")
            aliases[0].fullLink shouldEqual INBOX_ALIAS.fullLink
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
    fun `'findAliasesWithFullLinkMatchingEntireInput' should return only aliases matching terms if input not empty`() {
        whenever(
            aliasRepo.findWithAllOfTermsInFullLink(
                terms = eq(listOf("google")),
                lastTermIsPrefix = eq(false),
                offset = any(),
                limit = any()
            )
        ).thenReturn(emptyList())
        whenever(
            aliasRepo.countWithAllOfTermsInFullLink(eq(listOf("google")))
        ).thenReturn(0)

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
        whenever(aliasRepo.findByFullLink(INBOX_ALIAS.fullLink))
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
        whenever(aliasRepo.findByFullLink("inbox test"))
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