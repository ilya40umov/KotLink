@file:Suppress("NoItParamInMultilineLambda")
package org.kotlink.core.account

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.kotlink.core.exposed.NoKeyGeneratedException
import org.kotlink.core.exposed.RecordNotFoundException
import org.springframework.stereotype.Repository
import org.jetbrains.exposed.sql.Alias as ExposedAlias

interface UserAccountRepo {

    fun findById(id: Long): UserAccount?

    fun findByUserEmail(userEmail: String): UserAccount?

    fun insert(userAccount: UserAccount): UserAccount
}

@Repository
class UserAccountRepoImpl : UserAccountRepo {

    override fun findById(id: Long): UserAccount? =
        UserAccounts
            .select { UserAccounts.id.eq(id) }
            .map { it.asUserAccount() }
            .firstOrNull()

    override fun findByUserEmail(userEmail: String): UserAccount? =
        UserAccounts
            .select { UserAccounts.email.eq(userEmail) }
            .map { it.asUserAccount() }
            .firstOrNull()

    override fun insert(userAccount: UserAccount): UserAccount {
        val aliasId = UserAccounts.insert {
            it[email] = userAccount.email
        }.generatedKey
            ?: throw NoKeyGeneratedException("No primary key generated for user account '${userAccount.email}'")
        return findById(aliasId.toLong())
            ?: throw RecordNotFoundException("Created user account #${userAccount.id} not found")
    }
}

internal object UserAccounts : Table("user_account") {
    val id = long("id").autoIncrement("user_account_id_seq").primaryKey()
    val email = varchar("email", length = 1024)
}

internal fun ResultRow.asUserAccount() = UserAccount(
    id = this[UserAccounts.id],
    email = this[UserAccounts.email]
)

internal fun ResultRow.asUserAccount(alias: ExposedAlias<UserAccounts>) = UserAccount(
    id = this[alias[UserAccounts.id]],
    email = this[alias[UserAccounts.email]]
)