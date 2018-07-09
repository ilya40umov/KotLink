package org.kotlink.core.namespace

import org.kotlink.core.alias.AliasRepo
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class NamespaceService(
    private val aliasRepo: AliasRepo,
    private val namespaceRepo: NamespaceRepo
) {

    fun findAll(): List<Namespace> = namespaceRepo.findAll()

    fun findById(id: Long): Namespace? = namespaceRepo.findById(id)

    fun create(namespace: Namespace): Namespace {
        verifyKeywordNotTaken(namespace.keyword)
        return namespaceRepo.insert(namespace)
    }

    fun update(namespace: Namespace): Namespace {
        val foundNamespace = namespaceRepo.findByIdOrThrow(namespace.id)
        if (foundNamespace.keyword.isEmpty()) {
            throw UntouchableNamespaceException("Default namespace can't be edited")
        }
        if (namespace.keyword != foundNamespace.keyword) {
            verifyKeywordNotTaken(namespace.keyword)
        }
        return namespaceRepo.update(namespace)
    }

    fun deleteById(id: Long): Namespace {
        val foundNamespace = namespaceRepo.findByIdOrThrow(id)
        if (foundNamespace.keyword.isEmpty()) {
            throw UntouchableNamespaceException("Default namespace can't be removed")
        }
        aliasRepo.findByNamespace(foundNamespace.keyword).also {
            if (it.isNotEmpty()) {
                throw UntouchableNamespaceException(
                    "Namespace '${foundNamespace.keyword}' still contains ${it.size} aliases")
            }
        }
        namespaceRepo.deleteById(id)
        return foundNamespace
    }

    private fun verifyKeywordNotTaken(keyword: String) {
        if (namespaceRepo.findByKeyword(keyword) != null) {
            throw KeywordTakenException("Keyword '$keyword' is already taken")
        }
    }
}