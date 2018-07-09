package org.kotlink.ui.secret

import org.kotlink.core.secret.ApiSecretService
import org.kotlink.ui.CurrentUser
import org.kotlink.ui.SelectView
import org.kotlink.ui.UiView
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/ui/extension_secret")
class ExtensionSecretUiController(private val apiSecretService: ApiSecretService) {

    @GetMapping
    @SelectView(UiView.EXTENSION_SECRET)
    fun showIndividualExtensionSecret(model: Model, currentUser: CurrentUser): String {
        val currentUserEmail = currentUser.getEmail()
            ?: throw AccessDeniedException("You must be authenticated to access this endpoint!")
        model.addAttribute("apiSecret", apiSecretService.findOrCreateForEmail(currentUserEmail))
        return "secret/extension_secret"
    }
}
