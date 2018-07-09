package org.kotlink.core.namespace

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.amshove.kluent.any
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldThrow
import org.junit.Test
import org.junit.runner.RunWith
import org.kotlink.ABC_NAMESPACE
import org.kotlink.DEFAULT_NAMESPACE
import org.kotlink.INBOX_ALIAS
import org.kotlink.core.alias.AliasRepo
import org.kotlink.core.exposed.RecordNotFoundException
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class NamespaceServiceTest {

    private val aliasRepo = mock<AliasRepo>()
    private val namespaceRepo = mock<NamespaceRepo>()
    private val service = NamespaceService(aliasRepo, namespaceRepo)

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
            it.keyword shouldEqual ABC_NAMESPACE.keyword
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
    fun `'update' should throw exception if new keyword is taken`() {
        whenever(namespaceRepo.findByIdOrThrow(ABC_NAMESPACE.id))
            .thenReturn(ABC_NAMESPACE)
        whenever(namespaceRepo.findByKeyword("new"))
            .thenReturn(ABC_NAMESPACE);

        { service.update(ABC_NAMESPACE.copy(keyword = "new")) } shouldThrow KeywordTakenException::class
    }

    @Test
    fun `'update' should return updated namespace if provided namespace is valid`() {
        whenever(namespaceRepo.findByIdOrThrow(ABC_NAMESPACE.id))
            .thenReturn(ABC_NAMESPACE)
        whenever(namespaceRepo.update(any()))
            .thenReturn(ABC_NAMESPACE)

        service.update(ABC_NAMESPACE).also {
            it.keyword shouldEqual ABC_NAMESPACE.keyword
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
    fun `'deleteById' should throw exception if there are aliases associated with namespace`() {
        whenever(namespaceRepo.findByIdOrThrow(ABC_NAMESPACE.id))
            .thenReturn(ABC_NAMESPACE)
        whenever(aliasRepo.findByNamespace(ABC_NAMESPACE.keyword))
            .thenReturn(listOf(INBOX_ALIAS));

        { service.deleteById(ABC_NAMESPACE.id) } shouldThrow UntouchableNamespaceException::class
    }

    @Test
    fun `'deleteById' should return deleted namespace if provided namespace was found`() {
        whenever(namespaceRepo.findByIdOrThrow(ABC_NAMESPACE.id))
            .thenReturn(ABC_NAMESPACE)
        whenever(aliasRepo.findByNamespace(ABC_NAMESPACE.keyword))
            .thenReturn(emptyList())

        service.deleteById(ABC_NAMESPACE.id).also {
            it.keyword shouldEqual ABC_NAMESPACE.keyword
        }
    }
}