package com.ilya40umov.golink.core

import com.ilya40umov.golink.alias.Alias
import com.ilya40umov.golink.alias.AliasRepository
import org.springframework.stereotype.Service

@Service
class GoLinkService(private val aliasRepository: AliasRepository) {

    fun findRedirectUrlByLink(userProvidedLink: String): String? =
        aliasRepository.findByFullLink(userProvidedLink)
            .let { it?.redirectUrl }

    fun suggestAliasesByLinkPrefix(userProvidedLinkPrefix: String): OpenSearchSuggestions {
        val aliases = aliasRepository.findByFullLinkPrefix(userProvidedLinkPrefix)
        val fullLinks = aliases.map { it.fullLink }
        return OpenSearchSuggestions(
            prefix = userProvidedLinkPrefix,
            links = fullLinks,
            descriptions = fullLinks,
            redirectUrls = aliases.map { it.redirectUrl }
        )
    }

    fun searchAliasesMatchingInput(userProvidedInput: String): List<Alias> {
        val keywords = userProvidedInput.split("\\s+".toRegex())
        return aliasRepository.findWithAtLeastOneOfKeywords(keywords)
    }
}