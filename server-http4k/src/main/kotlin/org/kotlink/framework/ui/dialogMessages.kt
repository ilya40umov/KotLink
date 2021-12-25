package org.kotlink.framework.ui

import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.filter.flash
import org.http4k.filter.withFlash

fun Response.withSuccessMessage(message: String): Response =
    withFlash("success_message:$message")

fun Request.successMessage(): String? {
    val flash = flash()
    return when {
        flash != null && flash.startsWith("success_message:") -> flash.substringAfter(":")
        else -> null
    }
}

fun Response.withErrorMessage(message: String): Response =
    withFlash("error_message:$message")

fun Request.errorMessage(): String? {
    val flash = flash()
    return when {
        flash != null && flash.startsWith("error_message:") -> flash.substringAfter(":")
        else -> null
    }
}