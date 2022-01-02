package org.kotlink.domain.namespace

import org.kotlink.domain.getString
import org.kotlink.framework.dynamodb.asAttributeValue
import org.kotlink.framework.dynamodb.asAttributeValueUpdate
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest
import software.amazon.awssdk.services.dynamodb.model.QueryRequest
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest

class NamespaceRepository(
    private val dbClient: DynamoDbClient,
    private val tableName: String
) {
    fun findAll(): List<Namespace> {
        val response = dbClient.query(
            QueryRequest.builder()
                .tableName(tableName)
                .keyConditionExpression("$PRIMARY_KEY_FIELD = :pk")
                .expressionAttributeValues(mapOf(":pk" to NAMESPACES_PK.asAttributeValue()))
                .build()
        )
        return response.items().map { it.toNamespace() }
    }

    fun findById(id: String): Namespace? {
        val response = dbClient.getItem(
            GetItemRequest.builder()
                .tableName(tableName)
                .key(
                    mapOf(
                        PRIMARY_KEY_FIELD to NAMESPACES_PK.asAttributeValue(),
                        SORT_KEY_FIELD to id.asAttributeValue()
                    )
                )
                .build()
        )
        return if (response.hasItem()) response.item().toNamespace() else null
    }

    fun create(namespace: Namespace) {
        dbClient.putItem(
            PutItemRequest.builder()
                .tableName(tableName)
                .item(
                    mapOf(
                        PRIMARY_KEY_FIELD to NAMESPACES_PK.asAttributeValue(),
                        SORT_KEY_FIELD to namespace.id.asAttributeValue(),
                        LINK_PREFIX_FIELD to namespace.linkPrefix.asAttributeValue(),
                        DESCRIPTION_FIELD to namespace.description.asAttributeValue(),
                        OWNER_EMAIL_FIELD to namespace.ownerEmail.asAttributeValue()
                    )
                )
                .build()
        )
    }

    fun update(namespace: Namespace) {
        dbClient.updateItem(
            UpdateItemRequest.builder()
                .tableName(tableName)
                .key(
                    mapOf(
                        PRIMARY_KEY_FIELD to NAMESPACES_PK.asAttributeValue(),
                        SORT_KEY_FIELD to namespace.id.asAttributeValue()
                    )
                )
                .attributeUpdates(
                    mapOf(
                        LINK_PREFIX_FIELD to namespace.linkPrefix.asAttributeValueUpdate(),
                        DESCRIPTION_FIELD to namespace.description.asAttributeValueUpdate(),
                        OWNER_EMAIL_FIELD to namespace.ownerEmail.asAttributeValueUpdate()
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
                        PRIMARY_KEY_FIELD to NAMESPACES_PK.asAttributeValue(),
                        SORT_KEY_FIELD to id.asAttributeValue()
                    )
                )
                .build()
        )
    }

    private fun Map<String, AttributeValue>.toNamespace() = Namespace(
        linkPrefix = getString(LINK_PREFIX_FIELD),
        description = getString(DESCRIPTION_FIELD),
        ownerEmail = getString(OWNER_EMAIL_FIELD),
    )

    companion object {
        private const val PRIMARY_KEY_FIELD = "PK"
        private const val SORT_KEY_FIELD = "SK"
        private const val LINK_PREFIX_FIELD = "LinkPrefix"
        private const val DESCRIPTION_FIELD = "Description"
        private const val OWNER_EMAIL_FIELD = "OwnerEmail"

        private const val NAMESPACES_PK = "namespaces"
    }
}