package org.kotlink.api.namespace

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.kotlink.core.dao.NoKeyGeneratedException
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

interface NamespaceRepo {

    fun findAll(): List<Namespace>

    fun findById(id: Long): Namespace?

    fun findByKeyword(keyword: String): Namespace?

    fun insert(namespace: Namespace): Long

    fun deleteById(id: Long): Boolean
}

@Repository
@Transactional
class NamespaceRepoImpl : NamespaceRepo {

    override fun findAll() =
        Namespaces.selectAll()
            .map { it.asNamespace() }

    override fun findById(id: Long) =
        Namespaces.select { Namespaces.id.eq(id) }
            .map { it.asNamespace() }
            .firstOrNull()

    override fun findByKeyword(keyword: String): Namespace? =
        Namespaces.select { Namespaces.keyword.eq(keyword) }
            .map { it.asNamespace() }
            .firstOrNull()

    override fun insert(namespace: Namespace): Long {
        val namespaceId = Namespaces.insert {
            it[keyword] = namespace.keyword
        }.generatedKey
        return namespaceId?.toLong() ?: throw NoKeyGeneratedException()
    }

    override fun deleteById(id: Long) =
        Namespaces.deleteWhere { Namespaces.id.eq(id) } > 0
}

private object Namespaces : Table("namespace") {
    val id = long("id").autoIncrement("namespace_id_seq").primaryKey()
    val keyword = varchar("keyword", length = 128)
}

private fun ResultRow.asNamespace() = Namespace(
    id = this[Namespaces.id],
    keyword = this[Namespaces.keyword]
)
