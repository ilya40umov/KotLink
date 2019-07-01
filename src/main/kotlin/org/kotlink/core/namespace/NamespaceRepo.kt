@file:Suppress("NoItParamInMultilineLambda")

package org.kotlink.core.namespace

import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.alias
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.kotlink.core.account.UserAccounts
import org.kotlink.core.account.asUserAccount
import org.kotlink.core.alias.Aliases
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
        Namespaces.withJoins
            .selectAll()
            .orderBy(Namespaces.keyword, order = SortOrder.ASC)
            .map { it.asNamespace() }

    override fun findById(id: Long) =
        Namespaces.withJoins
            .select { Namespaces.id.eq(id) }
            .map { it.asNamespace() }
            .firstOrNull()

    override fun findByKeyword(keyword: String): Namespace? =
        Namespaces.withJoins
            .select { Namespaces.keyword.eq(keyword) }
            .map { it.asNamespace() }
            .firstOrNull()

    override fun insert(namespace: Namespace): Namespace {
        val namespaceId = Namespaces.insert {
            it[keyword] = namespace.keyword
            it[description] = namespace.description
            it[ownerAccountId] = namespace.ownerAccount.id
        }.let {
            it[Aliases.id]
        }
        return findById(namespaceId) ?: throw RecordNotFoundException("Inserted namespace #$namespaceId not found")
    }

    override fun update(namespace: Namespace): Namespace {
        Namespaces.update({ Namespaces.id.eq(namespace.id) }) {
            it[keyword] = namespace.keyword
            it[description] = namespace.description
            it[ownerAccountId] = namespace.ownerAccount.id
        }
        return findById(namespace.id)
            ?: throw RecordNotFoundException("Updated namespace #${namespace.id} not found")
    }

    override fun deleteById(id: Long) =
        Namespaces.deleteWhere { Namespaces.id.eq(id) } > 0
}

internal object Namespaces : Table("namespace") {
    val id = long("id").autoIncrement("namespace_id_seq").primaryKey()
    val keyword = varchar("keyword", length = Namespace.MAX_KEYWORD_LENGTH)
    val description = varchar("description", length = Namespace.MAX_DESCRIPTION_LENGTH)
    val ownerAccountId = long("owner_account_id") references UserAccounts.id

    val userAccountsAlias = UserAccounts.alias("ns_owner")
    val withJoins =
        join(userAccountsAlias, JoinType.LEFT, ownerAccountId, userAccountsAlias[UserAccounts.id])
}

internal fun ResultRow.asNamespace() = Namespace(
    id = this[Namespaces.id],
    keyword = this[Namespaces.keyword],
    description = this[Namespaces.description],
    ownerAccount = this.asUserAccount(Namespaces.userAccountsAlias)
)
