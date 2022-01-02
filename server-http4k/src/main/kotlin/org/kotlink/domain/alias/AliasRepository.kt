package org.kotlink.domain.alias

import org.kotlink.domain.getString
import org.kotlink.framework.dynamodb.asAttributeValue
import org.kotlink.framework.dynamodb.asAttributeValueUpdate
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.BatchGetItemRequest
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest
import software.amazon.awssdk.services.dynamodb.model.KeysAndAttributes
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest
import software.amazon.awssdk.services.dynamodb.model.ScanRequest
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest

class AliasRepository(
    private val dbClient: DynamoDbClient,
    private val tableName: String
) {
    fun findById(id: String): Alias? {
        val response = dbClient.getItem(
            GetItemRequest.builder()
                .tableName(tableName)
                .key(
                    mapOf(
                        PRIMARY_KEY_FIELD to "$ALIAS_PK_PREFIX$id".asAttributeValue(),
                        SORT_KEY_FIELD to id.asAttributeValue()
                    )
                )
                .build()
        )
        return if (response.hasItem()) response.item().toAlias() else null
    }

    fun findAll(): List<Alias> {
        val response = dbClient.scan(
            ScanRequest.builder()
                .tableName(tableName)
                .filterExpression("begins_with($PRIMARY_KEY_FIELD, :pk_prefix)")
                .expressionAttributeValues(mapOf(":pk_prefix" to ALIAS_PK_PREFIX.asAttributeValue()))
                .build()
        )
        return response.items().map { it.toAlias() }
    }

    fun findByIds(ids: List<String>): List<Alias> {
        val response = dbClient.batchGetItem(
            BatchGetItemRequest.builder()
                .requestItems(
                    mapOf(
                        tableName to KeysAndAttributes.builder().keys(
                            ids.map { id ->
                                mapOf(
                                    PRIMARY_KEY_FIELD to "$ALIAS_PK_PREFIX$id".asAttributeValue(),
                                    SORT_KEY_FIELD to id.asAttributeValue()
                                )
                            }
                        ).build()
                    )
                )
                .build()
        )
        return response.responses()[tableName]?.map { it.toAlias() } ?: emptyList()
    }

    fun create(alias: Alias) {
        dbClient.putItem(
            PutItemRequest.builder()
                .tableName(tableName)
                .item(
                    mapOf(
                        PRIMARY_KEY_FIELD to "$ALIAS_PK_PREFIX${alias.id}".asAttributeValue(),
                        SORT_KEY_FIELD to alias.id.asAttributeValue(),
                        LINK_PREFIX_FIELD to alias.linkPrefix.asAttributeValue(),
                        LINK_FIELD to alias.link.asAttributeValue(),
                        REDIRECT_URL_FIELD to alias.redirectUrl.asAttributeValue(),
                        DESCRIPTION_FIELD to alias.description.asAttributeValue(),
                        OWNER_EMAIL_FIELD to alias.ownerEmail.asAttributeValue()
                    )
                )
                .build()
        )
    }

    fun update(alias: Alias) {
        dbClient.updateItem(
            UpdateItemRequest.builder()
                .tableName(tableName)
                .key(
                    mapOf(
                        PRIMARY_KEY_FIELD to "$ALIAS_PK_PREFIX${alias.id}".asAttributeValue(),
                        SORT_KEY_FIELD to alias.id.asAttributeValue()
                    )
                )
                .attributeUpdates(
                    mapOf(
                        LINK_PREFIX_FIELD to alias.linkPrefix.asAttributeValueUpdate(),
                        LINK_FIELD to alias.link.asAttributeValueUpdate(),
                        REDIRECT_URL_FIELD to alias.redirectUrl.asAttributeValueUpdate(),
                        DESCRIPTION_FIELD to alias.description.asAttributeValueUpdate(),
                        OWNER_EMAIL_FIELD to alias.ownerEmail.asAttributeValueUpdate()
                    )
                )
                .build()
        )
    }

    fun deleteById(id: String) {
        dbClient.deleteItem(
            DeleteItemRequest.builder()
                .tableName(tableName)
                .key(
                    mapOf(
                        PRIMARY_KEY_FIELD to "$ALIAS_PK_PREFIX$id".asAttributeValue(),
                        SORT_KEY_FIELD to id.asAttributeValue()
                    )
                )
                .build()
        )
    }

    private fun Map<String, AttributeValue>.toAlias() = Alias(
        linkPrefix = getString(LINK_PREFIX_FIELD),
        link = getString(LINK_FIELD),
        redirectUrl = getString(REDIRECT_URL_FIELD),
        description = getString(DESCRIPTION_FIELD),
        ownerEmail = getString(OWNER_EMAIL_FIELD),
    )

    companion object {
        private const val PRIMARY_KEY_FIELD = "PK"
        private const val SORT_KEY_FIELD = "SK"
        private const val LINK_PREFIX_FIELD = "LinkPrefix"
        private const val LINK_FIELD = "Link"
        private const val REDIRECT_URL_FIELD = "RedirectUrl"
        private const val DESCRIPTION_FIELD = "Description"
        private const val OWNER_EMAIL_FIELD = "OwnerEmail"

        private const val ALIAS_PK_PREFIX = "alias:"
    }
}