package org.kotlink.framework.dynamodb

import software.amazon.awssdk.services.dynamodb.model.AttributeAction
import software.amazon.awssdk.services.dynamodb.model.AttributeValueUpdate

fun String.asAttributeValueUpdate(action: AttributeAction = AttributeAction.PUT): AttributeValueUpdate =
    AttributeValueUpdate.builder().action(action).value(asAttributeValue()).build()