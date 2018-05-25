package org.kotlink.ui

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/ui/help")
class HelpController {

    @GetMapping
    @SelectView(UiView.HELP)
    fun listNamespaces(model: Model): String {
        return "help"
    }
}
