package org.kotlink.core.namespace

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class NamespaceService(private val namespaceRepo: NamespaceRepo) {

    fun findAll(): List<Namespace> = namespaceRepo.findAll()

    fun findById(id: Long): Namespace? = namespaceRepo.findById(id)

    fun findByKeyword(keyword: String): Namespace? = namespaceRepo.findByKeyword(keyword)

    fun create(namespace: Namespace): Namespace {
        // TODO perform validation to check if we can create a namespace with the provided keyword
        return namespaceRepo.insert(namespace)
    }

    fun update(namespace: Namespace): Namespace {
        // TODO perform validation
        return namespaceRepo.update(namespace)
    }

    fun deleteById(id: Long): Boolean = namespaceRepo.deleteById(id)
}