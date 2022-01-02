package org.kotlink.domain.account

import org.kotlink.domain.getString
import org.kotlink.framework.dynamodb.asAttributeValue
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest

class UserAccountRepository(
    private val dbClient: DynamoDbClient,
    private val tableName: String
) {

    fun findByUserEmail(userEmail: String): UserAccount? {
        val response = dbClient.getItem(
            GetItemRequest.builder()
                .tableName(tableName)
                .key(
                    mapOf(
                        PRIMARY_KEY_FIELD to "$ACCOUNT_PK_PREFIX$userEmail".asAttributeValue(),
                        SORT_KEY_FIELD to userEmail.asAttributeValue()
                    )
                )
                .build()
        )
        return if (response.hasItem()) response.item().toUserAccount() else null
    }

    fun createAccount(account: UserAccount) {
        dbClient.putItem(
            PutItemRequest.builder()
                .tableName(tableName)
                .item(
                    mapOf(
                        PRIMARY_KEY_FIELD to "$ACCOUNT_PK_PREFIX${account.email}".asAttributeValue(),
                        SORT_KEY_FIELD to account.email.asAttributeValue(),
                        API_SECRET_FIELD to account.apiSecret.asAttributeValue()
                    )
                )
                .build()
        )
    }

    private fun Map<String, AttributeValue>.toUserAccount() = UserAccount(
        email = getString(SORT_KEY_FIELD),
        apiSecret = getString(API_SECRET_FIELD)
    )

    companion object {
        private const val PRIMARY_KEY_FIELD = "PK"
        private const val SORT_KEY_FIELD = "SK"
        private const val API_SECRET_FIELD = "ApiSecret"

        private const val ACCOUNT_PK_PREFIX = "account:"
    }
}