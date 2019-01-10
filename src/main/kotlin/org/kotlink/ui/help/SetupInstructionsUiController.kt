package org.kotlink.ui.help

import org.kotlink.core.CurrentUser
import org.kotlink.core.secret.ApiSecretService
import org.kotlink.ui.SelectView
import org.kotlink.ui.UiView
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/ui/setup_instructions")
class SetupInstructionsUiController(
    private val apiSecretService: ApiSecretService,
    private val currentUser: CurrentUser
) {

    @GetMapping
    @SelectView(UiView.SETUP_INSTRUCTIONS)
    fun showSetupInstructions(model: Model): String {
        model.addAttribute("apiSecret", apiSecretService.findOrCreateForEmail(currentUser.getEmail()))
        return "help/setup_instructions"
    }
}
