package org.kotlink.ui

import org.kotlink.core.CurrentUser
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class LoginController(
    private val currentUser: CurrentUser
) {
    @GetMapping("/login")
    fun loginRedirect(): String {
        if (currentUser.isKnown()) {
            return "redirect:/"
        }
        return "redirect:/oauth2/authorization/google"
    }
}