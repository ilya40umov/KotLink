package com.ilya40umov.golink.core

import mu.KotlinLogging
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
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
    fun redirectByAlias(@RequestParam("link") link: String): RedirectView {
        return RedirectView(goLinkService.findRedirectUrlByLink(link) ?: "/go/search?input=$link").also {
            logger.info { "Performing redirect: $link => $it.url" }
        }
    }

    @GetMapping("/suggest")
    @ResponseBody
    fun suggestAliases(
        @RequestParam("link") linkPrefix: String,
        @RequestParam("mode", required = false, defaultValue = "simple") mode: String): Any {
        val suggestions = goLinkService.suggestAliasesByLinkPrefix(linkPrefix)
        logger.info { "Suggested for $linkPrefix - ${suggestions.links}" }
        return when (mode) {
            "opensearch" -> suggestions
            "simple" -> suggestions.links
            else -> {
                logger.warn { "Unrecognized suggestion mode: $mode" }
                suggestions.links
            }
        }
    }

    @GetMapping("/search")
    fun searchLinks(@RequestParam("input") input: String, model: Model): String {
        val searchResults = goLinkService.searchAliasesMatchingInput(input)
        model.addAttribute("input", input)
        model.addAttribute("aliases", searchResults)
        return "search"
    }
}