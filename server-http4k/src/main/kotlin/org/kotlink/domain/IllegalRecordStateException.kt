package org.kotlink.domain

import software.amazon.awssdk.services.dynamodb.model.AttributeValue

class IllegalRecordStateException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

fun Map<String, AttributeValue>.getString(attribute: String): String =
    get(attribute)?.s() ?: throw IllegalRecordStateException("Missing required attribute $attribute")

fun Map<String, AttributeValue>.getStringSet(attribute: String): List<String> =
    get(attribute)?.ss() ?: throw IllegalRecordStateException("Missing required attribute $attribute")