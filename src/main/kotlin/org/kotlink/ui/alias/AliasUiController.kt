package org.kotlink.ui.alias

import org.kotlink.ui.SelectView
import org.kotlink.ui.UiView
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/ui/alias")
class AliasUiController {

    @GetMapping
    @SelectView(UiView.LIST_ALIASES)
    fun listNamespaces(model: Model): String {
        return "alias/list"
    }
}