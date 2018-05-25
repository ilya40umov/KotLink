package org.kotlink.api.resolution

import mu.KLogging
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.servlet.view.RedirectView

@Controller
@RequestMapping("/api/link")
class LinkResolutionController(private val linkResolutionService: LinkResolutionService) {

    @GetMapping("/redirect")
    fun redirectByAlias(@RequestParam("link") link: String): RedirectView {
        return RedirectView(linkResolutionService.findRedirectUrlByLink(link) ?: "/ui/search?input=$link").also {
            logger.info { "Performing redirect: $link => $it.url" }
        }
    }

    @GetMapping("/suggest")
    @ResponseBody
    fun suggestAliases(
        @RequestParam("link") linkPrefix: String,
        @RequestParam("mode", required = false, defaultValue = "simple") mode: String): Any {
        val suggestions = linkResolutionService.suggestAliasesByLinkPrefix(linkPrefix)
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

    companion object: KLogging()
}