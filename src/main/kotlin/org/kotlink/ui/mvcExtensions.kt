package org.kotlink.ui

import org.kotlink.core.KotLinkException
import org.springframework.ui.Model
import org.springframework.web.servlet.mvc.support.RedirectAttributes

fun RedirectAttributes.addSuccessMessage(message: String) {
    addFlashAttribute("success_message", message)
}

fun RedirectAttributes.addErrorMessage(message: String) {
    addFlashAttribute("error_message", message)
}

fun RedirectAttributes.addErrorMessage(e: Exception) {
    val message = when (e) {
        is KotLinkException -> e.message
        else -> "${e.javaClass.canonicalName} occurred (see logs for more details), message: ${e.message}"
    }
    addFlashAttribute("error_message", message)
}

fun Model.addErrorMessage(message: String) {
    addAttribute("error_message", message)
}

fun Model.addErrorMessage(e: Exception) {
    val message = when (e) {
        is KotLinkException -> e.message
        else -> "${e.javaClass.canonicalName} occurred (see logs for more details), message: ${e.message}"
    }
    addAttribute("error_message", message)
}