package org.kotlink.ui

import org.springframework.ui.Model
import org.springframework.web.servlet.mvc.support.RedirectAttributes

fun RedirectAttributes.addSuccessMessage(message: String) {
    addFlashAttribute("success_message", message)
}

fun RedirectAttributes.addErrorMessage(message: String) {
    addFlashAttribute("error_message", message)
}

fun Model.addErrorMessage(message: String) {
    addAttribute("error_message", message)
}