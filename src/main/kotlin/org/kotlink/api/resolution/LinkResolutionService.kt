package org.kotlink.api.resolution

import org.kotlink.core.alias.AliasService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class LinkResolutionService(private val aliasService: AliasService) {

    fun findRedirectUrlByLink(userProvidedLink: String): String? =
        aliasService.findByFullLink(userProvidedLink)
            .let { it?.redirectUrl }

    fun suggestAliasesByLinkPrefix(userProvidedLinkPrefix: String): OpenSearchSuggestions {
        val aliases = aliasService.findByFullLinkPrefix(userProvidedLinkPrefix)
        return OpenSearchSuggestions(userProvidedLinkPrefix, aliases)
    }
}