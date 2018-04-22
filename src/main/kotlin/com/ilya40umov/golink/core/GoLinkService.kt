package com.ilya40umov.golink.core

import com.ilya40umov.golink.LINK_SEPARATOR
import com.ilya40umov.golink.alias.AliasRepository
import org.springframework.stereotype.Service

@Service
class GoLinkService(private val aliasRepository: AliasRepository) {

    fun findRedirectUrlByLink(userProvidedLink: String): String? =
        normalizeLink(userProvidedLink).let(aliasRepository::findByFullLink).let { it?.redirectUrl }

    fun suggestLinksByPrefix(userProvidedLinkPrefix: String): OpenSearchSuggestions =
        normalizeLink(userProvidedLinkPrefix).let { normalizedPrefix ->
            val aliases = aliasRepository.findByFullLinkPrefix(normalizedPrefix)
            val denormalizedLinks = aliases.map { it.fullLink }.map(this::denormalizeLink)
            OpenSearchSuggestions(
                prefix = normalizedPrefix,
                links = denormalizedLinks,
                descriptions = denormalizedLinks,
                // TODO create URLs in format /go/redirect?link=xyz instead of sending out redirect URLs
                redirectUrls = aliases.map { it.redirectUrl }
            )
        }

    private fun normalizeLink(link: String): String {
        return link.trim().replace("\\s+".toRegex(), LINK_SEPARATOR)
    }

    private fun denormalizeLink(goLink: String): String {
        return goLink.trim { it <= ' ' }.replace(LINK_SEPARATOR, " ")
    }
}