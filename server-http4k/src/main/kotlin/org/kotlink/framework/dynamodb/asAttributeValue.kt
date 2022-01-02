package org.kotlink.framework.dynamodb

import software.amazon.awssdk.services.dynamodb.model.AttributeValue

fun String.asAttributeValue(): AttributeValue = AttributeValue.builder().s(this).build()

fun Number.asAttributeValue(): AttributeValue = AttributeValue.builder().n(this.toString()).build()

fun Collection<String>.asAttributeValue(): AttributeValue = AttributeValue.builder().ss(this).build()