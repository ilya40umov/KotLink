package org.kotlink.api.resolution

import org.kotlink.api.alias.Alias
import org.kotlink.api.alias.AliasRepo
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class LinkResolutionService(private val aliasRepo: AliasRepo) {

    fun findRedirectUrlByLink(userProvidedLink: String): String? =
        aliasRepo.findByFullLink(userProvidedLink)
            .let { it?.redirectUrl }

    fun suggestAliasesByLinkPrefix(userProvidedLinkPrefix: String): OpenSearchSuggestions {
        val aliases = aliasRepo.findByFullLinkPrefix(userProvidedLinkPrefix)
        return OpenSearchSuggestions(userProvidedLinkPrefix, aliases)
    }

    fun searchAliasesMatchingInput(userProvidedInput: String): List<Alias> {
        val keywords = userProvidedInput.split("\\s+".toRegex())
        return aliasRepo.findWithAtLeastOneOfKeywords(keywords)
    }
}