package org.kotlink.domain.alias

import org.kotlink.domain.getStringSet
import org.kotlink.framework.dynamodb.asAttributeValue
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest
import software.amazon.awssdk.services.dynamodb.model.QueryRequest
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItem
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItemsRequest
import software.amazon.awssdk.services.dynamodb.model.Update

class FullLinkRepository(
    private val dbClient: DynamoDbClient,
    private val tableName: String
) {
    fun registerFullLink(fullLink: String, keywords: Collection<String>) {
        dbClient.transactWriteItems(
            TransactWriteItemsRequest.builder()
                .transactItems(keywords.map { keyword ->
                    val firstLetter = keyword.substring(0, 1)
                    TransactWriteItem.builder().update(
                        Update.builder()
                            .tableName(tableName)
                            .key(
                                mapOf(
                                    PRIMARY_KEY_FIELD to "${DICTIONARY_PK_PREFIX}$firstLetter".asAttributeValue(),
                                    SORT_KEY_FIELD to keyword.asAttributeValue()
                                )
                            )
                            .updateExpression("ADD #fullLink :fullLink")
                            .expressionAttributeNames(mapOf("#fullLink" to FULL_LINKS_FIELD))
                            .expressionAttributeValues(mapOf(":fullLink" to listOf(fullLink).asAttributeValue()))
                            .build()
                    ).build()
                })
                .build()
        )
    }

    fun unregisterFullLink(fullLink: String, keywords: Collection<String>) {
        dbClient.transactWriteItems(
            TransactWriteItemsRequest.builder()
                .transactItems(keywords.map { keyword ->
                    val firstLetter = keyword.substring(0, 1)
                    TransactWriteItem.builder().update(
                        Update.builder()
                            .tableName(tableName)
                            .key(
                                mapOf(
                                    PRIMARY_KEY_FIELD to "${DICTIONARY_PK_PREFIX}$firstLetter".asAttributeValue(),
                                    SORT_KEY_FIELD to keyword.asAttributeValue()
                                )
                            )
                            .updateExpression("DELETE #fullLink :fullLink")
                            .expressionAttributeNames(mapOf("#fullLink" to FULL_LINKS_FIELD))
                            .expressionAttributeValues(mapOf(":fullLink" to listOf(fullLink).asAttributeValue()))
                            .build()
                    ).build()
                })
                .build()
        )
    }

    fun getFullLinksContainingKeyword(keyword: String): List<String> {
        val firstLetter = keyword.substring(0, 1)
        val response = dbClient.getItem(
            GetItemRequest.builder()
                .tableName(tableName)
                .key(
                    mapOf(
                        PRIMARY_KEY_FIELD to "${DICTIONARY_PK_PREFIX}$firstLetter".asAttributeValue(),
                        SORT_KEY_FIELD to keyword.asAttributeValue()
                    )
                )
                .build()
        )
        return if (response.hasItem()) response.item().getStringSet(FULL_LINKS_FIELD) else emptyList()
    }

    fun getFullLinksMatchingKeywordPrefix(keywordPrefix: String, limit: Int): List<String> {
        val firstLetter = keywordPrefix.substring(0, 1)
        val response = dbClient.query(
            QueryRequest.builder()
                .tableName(tableName)
                .keyConditionExpression("($PRIMARY_KEY_FIELD = :pk) and begins_with($SORT_KEY_FIELD, :sk)")
                .filterExpression("size($FULL_LINKS_FIELD) > :zero")
                .expressionAttributeValues(
                    mapOf(
                        ":pk" to "${DICTIONARY_PK_PREFIX}$firstLetter".asAttributeValue(),
                        ":sk" to keywordPrefix.asAttributeValue(),
                        ":zero" to 0.asAttributeValue()
                    )
                )
                .limit(limit) // XXX we assume that we won't need more than limit rows to find enough suggestions
                .build()
        )
        return response.items().asSequence().flatMap { it.getStringSet(FULL_LINKS_FIELD) }.take(limit).toList()
    }

    companion object {
        private const val PRIMARY_KEY_FIELD = "PK"
        private const val SORT_KEY_FIELD = "SK"
        private const val FULL_LINKS_FIELD = "FullLinks"

        private const val DICTIONARY_PK_PREFIX = "dictionary:"
    }
}