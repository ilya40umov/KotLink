package org.kotlink.core.secret

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.kotlink.core.exposed.RecordNotFoundException
import org.springframework.stereotype.Repository

interface ApiSecretRepo {

    fun findBySecret(secret: String): ApiSecret?

    fun findByUserEmail(userEmail: String): ApiSecret?

    fun insert(apiSecret: ApiSecret): ApiSecret

}

@Repository
class ApiSecretRepoImpl : ApiSecretRepo {

    override fun findBySecret(secret: String): ApiSecret? =
        ApiSecrets.select { ApiSecrets.secret.eq(secret) }
            .map { it.asApiSecret() }
            .firstOrNull()

    override fun findByUserEmail(userEmail: String): ApiSecret? =
        ApiSecrets.select { ApiSecrets.userEmail.eq(userEmail) }
            .map { it.asApiSecret() }
            .firstOrNull()

    override fun insert(apiSecret: ApiSecret): ApiSecret {
        ApiSecrets.insert {
            it[secret] = apiSecret.secret
            it[userEmail] = apiSecret.userEmail
        }
        return findBySecret(apiSecret.secret)
            ?: throw RecordNotFoundException("Inserted api secret for user ${apiSecret.userEmail} was not found")
    }
}

internal object ApiSecrets : Table("api_secret") {
    val secret = varchar("secret", length = 64).primaryKey()
    val userEmail = varchar("user_email", length = 1024)
}

private fun ResultRow.asApiSecret() = ApiSecret(
    secret = this[ApiSecrets.secret],
    userEmail = this[ApiSecrets.userEmail]
)
