package org.kotlink.core.secret

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.kotlink.core.account.UserAccounts
import org.kotlink.core.account.asUserAccount
import org.kotlink.core.exposed.RecordNotFoundException
import org.springframework.stereotype.Repository

interface ApiSecretRepo {

    fun findBySecret(secret: String): ApiSecret?

    fun findByUserEmail(userEmail: String): ApiSecret?

    fun insert(apiSecret: ApiSecret): ApiSecret
}

@Repository
@Suppress("NoItParamInMultilineLambda")
class ApiSecretRepoImpl : ApiSecretRepo {

    override fun findBySecret(secret: String): ApiSecret? =
        (ApiSecrets leftJoin UserAccounts)
            .select { ApiSecrets.secret.eq(secret) }
            .map { it.asApiSecret() }
            .firstOrNull()

    override fun findByUserEmail(userEmail: String): ApiSecret? =
        (ApiSecrets leftJoin UserAccounts)
            .select { UserAccounts.email.eq(userEmail) }
            .map { it.asApiSecret() }
            .firstOrNull()

    override fun insert(apiSecret: ApiSecret): ApiSecret {
        ApiSecrets.insert {
            it[secret] = apiSecret.secret
            it[userAccountId] = apiSecret.userAccount.id
        }
        return findBySecret(apiSecret.secret)
            ?: throw RecordNotFoundException("Inserted api secret for user ${apiSecret.userAccount} was not found")
    }
}

internal object ApiSecrets : Table("api_secret") {
    val id = long("id").autoIncrement("api_secret_id_seq").primaryKey()
    val secret = varchar("secret", length = 64).primaryKey()
    val userAccountId = long("user_account_id") references UserAccounts.id
}

private fun ResultRow.asApiSecret() = ApiSecret(
    id = this[ApiSecrets.id],
    secret = this[ApiSecrets.secret],
    userAccount = this.asUserAccount()
)
