package org.kotlink.core.namespace

import org.kotlink.core.CurrentUser
import org.kotlink.core.OperationDeniedException
import org.kotlink.core.alias.AliasRepo
import org.kotlink.core.ipblock.EditOp
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class NamespaceService(
    private val aliasRepo: AliasRepo,
    private val namespaceRepo: NamespaceRepo,
    private val currentUser: CurrentUser
) {

    fun findAll(): List<Namespace> = namespaceRepo.findAll()

    fun findById(id: Long): Namespace? = namespaceRepo.findById(id)

    @EditOp
    fun create(namespace: Namespace): Namespace {
        verifyKeywordNotTaken(namespace.keyword)
        return namespaceRepo.insert(namespace)
    }

    @EditOp
    fun update(namespace: Namespace): Namespace {
        val foundNamespace = namespaceRepo.findByIdOrThrow(namespace.id)
        if (foundNamespace.keyword.isEmpty()) {
            throw UntouchableNamespaceException("Default namespace can't be edited")
        }
        foundNamespace.verifyCanModify(currentUser) {
            "Only the owner (${foundNamespace.ownerAccount.email}) can modify this namespace"
        }
        if (namespace.keyword != foundNamespace.keyword) {
            verifyKeywordNotTaken(namespace.keyword)
        }
        return namespaceRepo.update(namespace).also {
            if (namespace.keyword != foundNamespace.keyword) {
                aliasRepo.refreshFullLinksInNamespaceWithId(namespaceId = namespace.id)
            }
        }
    }

    @EditOp
    @Suppress("ThrowsCount")
    fun deleteById(id: Long): Namespace {
        val foundNamespace = namespaceRepo.findByIdOrThrow(id)
        if (foundNamespace.keyword.isEmpty()) {
            throw UntouchableNamespaceException("Default namespace can't be removed")
        }
        foundNamespace.verifyCanModify(currentUser) {
            "Only the owner (${foundNamespace.ownerAccount.email}) can delete this namespace"
        }
        aliasRepo.findByNamespace(foundNamespace.keyword).also { aliases ->
            if (aliases.isNotEmpty()) {
                throw UntouchableNamespaceException(
                    "Namespace '${foundNamespace.keyword}' still contains ${aliases.size} aliases"
                )
            }
        }
        namespaceRepo.deleteById(id)
        return foundNamespace
    }

    private fun Namespace.verifyCanModify(currentUser: CurrentUser, message: () -> String) {
        val isOwner = ownerAccount.id == currentUser.getAccount().id
        if (!isOwner && !currentUser.isAdmin()) {
            throw OperationDeniedException(message = message())
        }
    }

    private fun verifyKeywordNotTaken(keyword: String) {
        if (namespaceRepo.findByKeyword(keyword) != null) {
            throw KeywordTakenException("Keyword '$keyword' is already taken")
        }
    }
}