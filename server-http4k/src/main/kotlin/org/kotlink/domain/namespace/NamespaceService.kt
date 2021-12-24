package org.kotlink.domain.namespace

import java.util.concurrent.ConcurrentHashMap

class NamespaceService {

    private val namespaces = ConcurrentHashMap<String, Namespace>()

    init {
        namespaces += Namespace.DEFAULT_NAMESPACE_ID to Namespace(
            linkPrefix = "",
            description = "Default namespace",
            ownerEmail = "zorro@gmail.com"
        )
        namespaces += "abc" to Namespace(
            linkPrefix = "abc",
            description = "Abc",
            ownerEmail = "zorro@gmail.com"
        )
    }

    fun findAll(): List<Namespace> {
        return namespaces.values.toList()
    }

    fun findById(id: String): Namespace? = namespaces[id]

    fun findByLinkPrefix(linkPrefix: String): Namespace? {
        val id = linkPrefix.ifEmpty { Namespace.DEFAULT_NAMESPACE_ID }
        return namespaces[id]
    }

    fun create(namespace: Namespace) {
        namespaces += namespace.id to namespace
    }

    fun update(namespace: Namespace) {
        if (namespaces.containsKey(namespace.id)) {
            namespaces += namespace.id to namespace
        }
    }

    fun deleteById(id: String): Namespace {
        return namespaces.remove(id) ?: throw IllegalArgumentException("Namespace not found!")
    }
}