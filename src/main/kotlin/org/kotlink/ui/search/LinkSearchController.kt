package org.kotlink.ui.search

import org.kotlink.api.resolution.LinkResolutionService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping("/ui")
class LinkSearchController(private val linkResolutionService: LinkResolutionService) {

    @GetMapping("/search")
    fun searchLinks(@RequestParam("input", defaultValue = "") input: String, model: Model): String {
        val searchResults = linkResolutionService.searchAliasesMatchingInput(input)
        model.addAttribute("input", input)
        model.addAttribute("aliases", searchResults)
        return "search/results"
    }
}