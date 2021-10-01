package org.kotlink.api.resolution

import io.micrometer.core.instrument.MeterRegistry
import mu.KLogging
import org.kotlink.core.metrics.recording
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import org.springframework.web.servlet.view.RedirectView

@Controller
@RequestMapping("/api/link")
class LinkResolutionController(
    private val linkResolutionService: LinkResolutionService,
    meterRegistry: MeterRegistry
) {

    private val redirectTimer = meterRegistry.timer("kotlink.api.redirect_timer")
    private val suggestTimer = meterRegistry.timer("kotlink.api.suggest_timer")

    @GetMapping("/redirect")
    fun redirectByAlias(@RequestParam("link") link: String): RedirectView =
        redirectTimer.recording {
            RedirectView(
                linkResolutionService.findRedirectUrlByLink(link) ?: "/ui/search?input=$link"
            ).also {
                logger.info { "Performing redirect: $link => $it.url" }
            }
        }

    @GetMapping("/suggest")
    @ResponseBody
    fun suggestAliases(
        @RequestParam("link") linkPrefix: String,
        @RequestParam("mode", required = false, defaultValue = "simple") mode: String
    ): Any = suggestTimer.recording {
        val redirectUri = ServletUriComponentsBuilder
            .fromCurrentRequestUri()
            .replacePath("/api/link/redirect")
            .replaceQuery(null)
            .toUriString()
        val suggestions = linkResolutionService.suggestAliasesByLinkPrefix(linkPrefix, redirectUri)
        logger.info { "Suggested for $linkPrefix - ${suggestions.links}" }
        when (mode) {
            "opensearch" -> suggestions
            "simple" -> suggestions.links.zip(suggestions.descriptions)
            else -> {
                logger.warn { "Unrecognized suggestion mode: $mode" }
                suggestions.links
            }
        }
    }

    companion object : KLogging()
}
