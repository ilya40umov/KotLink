package org.kotlink.ui.secret

import org.kotlink.core.secret.ApiSecretService
import org.kotlink.core.CurrentUser
import org.kotlink.ui.SelectView
import org.kotlink.ui.UiView
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/ui/extension_secret")
class ExtensionSecretUiController(
    private val apiSecretService: ApiSecretService,
    private val currentUser: CurrentUser
) {

    @GetMapping
    @SelectView(UiView.EXTENSION_SECRET)
    fun showIndividualExtensionSecret(model: Model): String {
        model.addAttribute("apiSecret", apiSecretService.findOrCreateForEmail(currentUser.getEmail()))
        return "secret/extension_secret"
    }
}
