package org.kotlink.ui

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/ui/extension_secret")
class ExtensionSecretController {

    @GetMapping
    @SelectView(UiView.EXTENSION_SECRET)
    fun listNamespaces(model: Model): String {
        return "secret/extension_secret"
    }
}
