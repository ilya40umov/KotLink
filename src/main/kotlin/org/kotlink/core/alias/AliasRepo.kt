package org.kotlink.core.alias

import org.jetbrains.exposed.sql.ComparisonOp
import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.ExpressionWithColumnType
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.QueryParameter
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.alias
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.kotlink.core.account.UserAccounts
import org.kotlink.core.account.asUserAccount
import org.kotlink.core.exposed.NoKeyGeneratedException
import org.kotlink.core.exposed.RecordNotFoundException
import org.kotlink.core.namespace.Namespaces
import org.kotlink.core.namespace.asNamespace
import org.springframework.stereotype.Repository

interface AliasRepo {

    fun findAll(): List<Alias>

    fun findById(id: Long): Alias?

    fun findByIdOrThrow(id: Long): Alias =
        findById(id) ?: throw RecordNotFoundException("Alias #$id not found")

    fun findByNamespace(namespace: String): List<Alias>

    fun findByNamespacePrefix(namespacePrefix: String): List<Alias>

    fun findByNamespaceAndLink(namespace: String, link: String): Alias?

    fun findByNamespaceAndLinkPrefix(namespace: String, linkPrefix: String): List<Alias>

    fun findByNamespaceAndWithAtLeastOneOfTerms(namespace: String, terms: List<String>): List<Alias>

    fun findWithAtLeastOneOfTerms(terms: List<String>): List<Alias>

    fun insert(alias: Alias): Alias

    fun update(alias: Alias): Alias

    fun deleteById(id: Long): Boolean
}

@Repository
class AliasRepoImpl : AliasRepo {

    override fun findAll(): List<Alias> =
        Aliases.withJoins
            .selectAll()
            .orderBy(Namespaces.keyword, isAsc = true)
            .orderBy(Aliases.link, isAsc = true)
            .map { it.asAlias() }

    override fun findById(id: Long): Alias? =
        Aliases.withJoins
            .select { Aliases.id.eq(id) }
            .map { it.asAlias() }
            .firstOrNull()

    override fun findByNamespace(namespace: String): List<Alias> =
        Aliases.withJoins
            .select { Namespaces.keyword.eq(namespace) }
            .orderBy(Aliases.link, isAsc = true)
            .map { it.asAlias() }

    override fun findByNamespacePrefix(namespacePrefix: String): List<Alias> =
        Aliases.withJoins
            .select { Namespaces.keyword.like("$namespacePrefix%") }
            .orderBy(Namespaces.keyword, isAsc = true)
            .orderBy(Aliases.link, isAsc = true)
            .map { it.asAlias() }

    override fun findByNamespaceAndLink(namespace: String, link: String): Alias? =
        Aliases.withJoins
            .select { Namespaces.keyword.eq(namespace) and Aliases.link.eq(link) }
            .map { it.asAlias() }
            .firstOrNull()

    override fun findByNamespaceAndLinkPrefix(namespace: String, linkPrefix: String): List<Alias> =
        Aliases.withJoins
            .select { Namespaces.keyword.eq(namespace) and Aliases.link.like("$linkPrefix%") }
            .orderBy(Namespaces.keyword, isAsc = true)
            .orderBy(Aliases.link, isAsc = true)
            .map { it.asAlias() }

    private class PsqlRegexpOp(expr1: Expression<*>, expr2: Expression<*>) : ComparisonOp(expr1, expr2, "~*")

    private infix fun <T : String?> ExpressionWithColumnType<T>.psqlRegexp(pattern: String): Op<Boolean> =
        PsqlRegexpOp(this, QueryParameter(pattern, columnType))

    override fun findWithAtLeastOneOfTerms(terms: List<String>): List<Alias> {
        val regexp = terms.joinToString("|")
        return Aliases.withJoins
            .select { Aliases.link.psqlRegexp(regexp) or Aliases.description.psqlRegexp(regexp) }
            .orderBy(Namespaces.keyword, isAsc = true)
            .orderBy(Aliases.link, isAsc = true)
            .map { it.asAlias() }
    }

    override fun findByNamespaceAndWithAtLeastOneOfTerms(namespace: String, terms: List<String>): List<Alias> {
        val regexp = terms.joinToString("|")
        return Aliases.withJoins
            .select {
                Namespaces.keyword.eq(namespace) and
                    (Aliases.link.psqlRegexp(regexp) or Aliases.description.psqlRegexp(regexp))
            }
            .orderBy(Namespaces.keyword, isAsc = true)
            .orderBy(Aliases.link, isAsc = true)
            .map { it.asAlias() }
    }

    override fun insert(alias: Alias): Alias {
        val aliasId = Aliases.insert {
            it[namespaceId] = alias.namespace.id
            it[link] = alias.link
            it[redirectUrl] = alias.redirectUrl
            it[description] = alias.description
            it[ownerAccountId] = alias.ownerAccount.id
        }.generatedKey ?: throw NoKeyGeneratedException("No primary key generated for alias '${alias.fullLink}'")
        return findById(aliasId.toLong())
            ?: throw RecordNotFoundException("Created alias #${alias.id} not found")
    }

    override fun update(alias: Alias): Alias {
        Aliases.update({ Aliases.id.eq(alias.id) }) {
            it[link] = alias.link
            it[redirectUrl] = alias.redirectUrl
            it[description] = alias.description
            it[ownerAccountId] = alias.ownerAccount.id
        }
        return findById(alias.id)
            ?: throw RecordNotFoundException("Updated alias #${alias.id} not found")
    }

    override fun deleteById(id: Long): Boolean =
        Aliases.deleteWhere { Aliases.id.eq(id) } > 0
}

private object Aliases : Table("alias") {
    val id = long("id").autoIncrement("alias_id_seq").primaryKey()
    val namespaceId = long("namespace_id") references Namespaces.id
    val link = varchar("link", length = Alias.MAX_LINK_LENGTH)
    val redirectUrl = varchar("redirect_url", length = Alias.MAX_REDIRECT_URL_LENGTH)
    val description = varchar("description", length = Alias.MAX_DESCRIPTION_LENGTH)
    val ownerAccountId = long("owner_account_id") references UserAccounts.id

    val userAccountsAlias = UserAccounts.alias("alias_owner")
    val withJoins =
        (Aliases leftJoin Namespaces.withJoins)
            .join(userAccountsAlias, JoinType.LEFT, Aliases.ownerAccountId, userAccountsAlias[UserAccounts.id])
}

private fun ResultRow.asAlias() = Alias(
    id = this[Aliases.id],
    namespace = this.asNamespace(),
    link = this[Aliases.link],
    redirectUrl = this[Aliases.redirectUrl],
    description = this[Aliases.description],
    ownerAccount = this.asUserAccount(Aliases.userAccountsAlias)
)
