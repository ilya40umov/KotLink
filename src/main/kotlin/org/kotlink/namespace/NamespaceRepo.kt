package org.kotlink.namespace

import org.springframework.stereotype.Repository

@Repository
class NamespaceRepo {

    private var nextId = 0L
    private val namespaces = mutableListOf<Namespace>()

    fun findAll(): List<Namespace> = namespaces.toList()

    fun findById(id: Long): Namespace? = namespaces.find { it.id == id }

    fun findByKeyword(keyword: String): Namespace? = namespaces.find { it.keyword == keyword }

    fun insert(namespace: Namespace): Namespace {
        nextId += 1
        return namespace.copy(id = nextId).also {
            namespaces.add(it)
        }
    }

    fun deleteById(id: Long) {
        namespaces.removeIf { it.id == id }
    }
}