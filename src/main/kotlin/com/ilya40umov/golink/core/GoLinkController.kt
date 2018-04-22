package com.ilya40umov.golink.core

import mu.KotlinLogging
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.servlet.view.RedirectView

@Controller
@RequestMapping("/go")
class GoLinkController(private val goLinkService: GoLinkService) {

    private val logger = KotlinLogging.logger {}

    @GetMapping("/redirect")
    fun redirectByLink(@RequestParam("link") link: String): RedirectView {
        return RedirectView(goLinkService.findRedirectUrlByLink(link) ?: "/go/search?input=$link").also {
            logger.info { "Performing redirect: $link => $it.url" }
        }
    }

    @GetMapping("/suggest")
    @ResponseBody
    fun suggestLinks(@RequestParam("link") linkPrefix: String): Any {
        return goLinkService.suggestLinksByPrefix(linkPrefix).also {
            logger.info { "Suggested for $linkPrefix - ${it.links}" }
        }
    }

    @GetMapping("/search")
    fun searchLinks(@RequestParam("input") input: String): String {
        return "search"
    }
}