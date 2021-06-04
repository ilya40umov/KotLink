package org.kotlink.core.namespace

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldThrow
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.kotlink.ABC_NAMESPACE
import org.kotlink.DEFAULT_NAMESPACE
import org.kotlink.INBOX_ALIAS
import org.kotlink.TEST_ACCOUNT
import org.kotlink.core.CurrentUser
import org.kotlink.core.OperationDeniedException
import org.kotlink.core.alias.AliasRepo
import org.kotlink.core.exposed.RecordNotFoundException
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class NamespaceServiceTest(
    @Mock private val aliasRepo: AliasRepo,
    @Mock private val namespaceRepo: NamespaceRepo,
    @Mock private val currentUser: CurrentUser
) {

    private val service = NamespaceService(aliasRepo, namespaceRepo, currentUser)

    @Test
    fun `'create' should throw exception if keyword is already taken`() {
        whenever(namespaceRepo.findByKeyword(ABC_NAMESPACE.keyword))
            .thenReturn(ABC_NAMESPACE);

        { service.create(ABC_NAMESPACE) } shouldThrow KeywordTakenException::class
    }

    @Test
    fun `'create' should return the created namespace if keyword is not taken`() {
        whenever(namespaceRepo.findByKeyword(ABC_NAMESPACE.keyword))
            .thenReturn(null)
        whenever(namespaceRepo.insert(any()))
            .thenReturn(ABC_NAMESPACE)

        service.create(ABC_NAMESPACE).also {
            it.keyword shouldBeEqualTo ABC_NAMESPACE.keyword
        }
    }

    @Test
    fun `'update' should throw exception if namespace can't be found`() {
        whenever(namespaceRepo.findByIdOrThrow(ABC_NAMESPACE.id))
            .thenThrow(RecordNotFoundException("Fake exception"));

        { service.update(ABC_NAMESPACE) } shouldThrow RecordNotFoundException::class
    }

    @Test
    fun `'update' should throw exception if it's called for the default namespace`() {
        whenever(namespaceRepo.findByIdOrThrow(DEFAULT_NAMESPACE.id))
            .thenReturn(DEFAULT_NAMESPACE);

        { service.update(DEFAULT_NAMESPACE) } shouldThrow UntouchableNamespaceException::class
    }

    @Test
    fun `'update' should throw exception if current user is not the owner`() {
        whenever(namespaceRepo.findByIdOrThrow(ABC_NAMESPACE.id))
            .thenReturn(ABC_NAMESPACE)
        whenever(currentUser.getAccount())
            .thenReturn(TEST_ACCOUNT.copy(id = 987));

        { service.update(ABC_NAMESPACE.copy(keyword = "new")) } shouldThrow OperationDeniedException::class
    }

    @Test
    fun `'update' should throw exception if new keyword is taken`() {
        whenever(namespaceRepo.findByIdOrThrow(ABC_NAMESPACE.id))
            .thenReturn(ABC_NAMESPACE)
        whenever(currentUser.getAccount())
            .thenReturn(TEST_ACCOUNT)
        whenever(namespaceRepo.findByKeyword("new"))
            .thenReturn(ABC_NAMESPACE);

        { service.update(ABC_NAMESPACE.copy(keyword = "new")) } shouldThrow KeywordTakenException::class
    }

    @Test
    fun `'update' should return updated namespace if provided namespace is valid`() {
        whenever(namespaceRepo.findByIdOrThrow(ABC_NAMESPACE.id))
            .thenReturn(ABC_NAMESPACE)
        whenever(currentUser.getAccount())
            .thenReturn(TEST_ACCOUNT)
        whenever(namespaceRepo.update(any()))
            .thenReturn(ABC_NAMESPACE)

        service.update(ABC_NAMESPACE).also {
            it.keyword shouldBeEqualTo ABC_NAMESPACE.keyword
        }
    }

    @Test
    fun `'update' should refresh full links in the namespace if provided namespace contains a changed keyword`() {
        whenever(namespaceRepo.findByIdOrThrow(ABC_NAMESPACE.id))
            .thenReturn(ABC_NAMESPACE)
        whenever(currentUser.getAccount())
            .thenReturn(TEST_ACCOUNT)
        whenever(namespaceRepo.update(any()))
            .thenReturn(ABC_NAMESPACE)

        service.update(ABC_NAMESPACE.copy(keyword = "abc888")).also {
            verify(aliasRepo).refreshFullLinksInNamespaceWithId(ABC_NAMESPACE.id)
        }
    }

    @Test
    fun `'update' should not refresh full links in the namespace if provided namespace has not changed its keyword`() {
        whenever(namespaceRepo.findByIdOrThrow(ABC_NAMESPACE.id))
            .thenReturn(ABC_NAMESPACE)
        whenever(currentUser.getAccount())
            .thenReturn(TEST_ACCOUNT)
        whenever(namespaceRepo.update(any()))
            .thenReturn(ABC_NAMESPACE)

        service.update(ABC_NAMESPACE).also {
            verify(aliasRepo, never()).refreshFullLinksInNamespaceWithId(ABC_NAMESPACE.id)
        }
    }

    @Test
    fun `'deleteById' should throw exception if namespace can't be found`() {
        whenever(namespaceRepo.findByIdOrThrow(ABC_NAMESPACE.id))
            .thenThrow(RecordNotFoundException("Fake exception"));

        { service.deleteById(ABC_NAMESPACE.id) } shouldThrow RecordNotFoundException::class
    }

    @Test
    fun `'deleteById' should throw exception if it's called for the default namespace`() {
        whenever(namespaceRepo.findByIdOrThrow(DEFAULT_NAMESPACE.id))
            .thenReturn(DEFAULT_NAMESPACE);

        { service.deleteById(DEFAULT_NAMESPACE.id) } shouldThrow UntouchableNamespaceException::class
    }

    @Test
    fun `'deleteById' should throw exception if the current user is not the owner`() {
        whenever(namespaceRepo.findByIdOrThrow(ABC_NAMESPACE.id))
            .thenReturn(ABC_NAMESPACE)
        whenever(currentUser.getAccount())
            .thenReturn(TEST_ACCOUNT.copy(id = 987));

        { service.deleteById(ABC_NAMESPACE.id) } shouldThrow OperationDeniedException::class
    }

    @Test
    fun `'deleteById' should throw exception if there are aliases associated with namespace`() {
        whenever(namespaceRepo.findByIdOrThrow(ABC_NAMESPACE.id))
            .thenReturn(ABC_NAMESPACE)
        whenever(currentUser.getAccount())
            .thenReturn(TEST_ACCOUNT)
        whenever(aliasRepo.findByNamespace(ABC_NAMESPACE.keyword))
            .thenReturn(listOf(INBOX_ALIAS));

        { service.deleteById(ABC_NAMESPACE.id) } shouldThrow UntouchableNamespaceException::class
    }

    @Test
    fun `'deleteById' should return deleted namespace if provided namespace was deleted with success`() {
        whenever(namespaceRepo.findByIdOrThrow(ABC_NAMESPACE.id))
            .thenReturn(ABC_NAMESPACE)
        whenever(currentUser.getAccount())
            .thenReturn(TEST_ACCOUNT)
        whenever(aliasRepo.findByNamespace(ABC_NAMESPACE.keyword))
            .thenReturn(emptyList())

        service.deleteById(ABC_NAMESPACE.id).also {
            it.keyword shouldBeEqualTo ABC_NAMESPACE.keyword
        }
    }
}