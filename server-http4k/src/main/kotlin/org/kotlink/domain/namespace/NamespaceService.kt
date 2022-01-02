package org.kotlink.domain.namespace

import org.kotlink.domain.RecordNotFoundException

class NamespaceService(
    private val namespaceRepository: NamespaceRepository
) {
    fun findAll(): List<Namespace> =
        namespaceRepository.findAll()

    fun findByIdOrThrow(id: String): Namespace =
        namespaceRepository.findById(id) ?: throw RecordNotFoundException("Namespace with ID '$id' not found!")

    fun findByLinkPrefix(linkPrefix: String): Namespace? =
        namespaceRepository.findById(linkPrefix.ifBlank { Namespace.DEFAULT_NAMESPACE_ID })

    fun create(namespace: Namespace) {
        namespaceRepository.create(namespace)
    }

    fun update(namespace: Namespace) {
        namespaceRepository.update(
            findByIdOrThrow(namespace.id).copy(
                description = namespace.description,
                ownerEmail = namespace.ownerEmail
            )
        )
    }

    fun deleteById(id: String): Namespace = findByIdOrThrow(id).also {
        namespaceRepository.deleteById(id)
    }
}