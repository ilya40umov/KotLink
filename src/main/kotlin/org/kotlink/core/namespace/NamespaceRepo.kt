package org.kotlink.core.namespace

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.kotlink.core.exposed.NoKeyGeneratedException
import org.kotlink.core.exposed.RecordNotFoundException
import org.springframework.stereotype.Repository

interface NamespaceRepo {

    fun findAll(): List<Namespace>

    fun findById(id: Long): Namespace?

    fun findByIdOrThrow(id: Long): Namespace =
        findById(id) ?: throw RecordNotFoundException("Namespace #$id not found")

    fun findByKeyword(keyword: String): Namespace?

    fun insert(namespace: Namespace): Namespace

    fun update(namespace: Namespace): Namespace

    fun deleteById(id: Long): Boolean
}

@Repository
class NamespaceRepoImpl : NamespaceRepo {

    override fun findAll() =
        Namespaces.selectAll()
            .orderBy(Namespaces.keyword, isAsc = true)
            .map { it.asNamespace() }

    override fun findById(id: Long) =
        Namespaces.select { Namespaces.id.eq(id) }
            .map { it.asNamespace() }
            .firstOrNull()

    override fun findByKeyword(keyword: String): Namespace? =
        Namespaces.select { Namespaces.keyword.eq(keyword) }
            .map { it.asNamespace() }
            .firstOrNull()

    override fun insert(namespace: Namespace): Namespace {
        val namespaceId = Namespaces.insert {
            it[keyword] = namespace.keyword
            it[description] = namespace.description
        }.generatedKey ?: throw NoKeyGeneratedException("No primary key generated for namespace '${namespace.keyword}'")
        return findById(namespaceId.toLong())
            ?: throw RecordNotFoundException("Inserted namespace #$namespaceId not found")
    }

    override fun update(namespace: Namespace): Namespace {
        Namespaces.update({ Namespaces.id.eq(namespace.id) }) {
            it[keyword] = namespace.keyword
            it[description] = namespace.description
        }
        return findById(namespace.id)
            ?: throw RecordNotFoundException("Updated namespace #${namespace.id} not found")
    }

    override fun deleteById(id: Long) =
        Namespaces.deleteWhere { Namespaces.id.eq(id) } > 0
}

internal object Namespaces : Table("namespace") {
    val id = long("id").autoIncrement("namespace_id_seq").primaryKey()
    val keyword = varchar("keyword", length = 128)
    val description = varchar("description", length = 512)
}

internal fun ResultRow.asNamespace() = Namespace(
    id = this[Namespaces.id],
    keyword = this[Namespaces.keyword],
    description = this[Namespaces.description]
)
