package org.kotlink.domain.alias

import org.kotlink.domain.RecordNotFoundException

class AliasService(
    private val aliasRepository: AliasRepository,
    private val fullLinkRepository: FullLinkRepository
) {
    fun findByIdOrThrow(id: String): Alias =
        aliasRepository.findById(id) ?: throw RecordNotFoundException("Alias with ID '$id' not found!")

    fun findByFullLink(fullLink: String): Alias? =
        aliasRepository.findById(id = Alias.computeId(fullLink))

    fun findStartingWithPrefix(fullLinkPrefix: String, limit: Int = 5): List<Alias> {
        val keywords = fullLinkPrefix.toKeywords()
        val lastKeywordIsPrefix = !fullLinkPrefix.endsWith(" ")
        val completeKeywords = when {
            lastKeywordIsPrefix -> keywords.subList(0, keywords.size - 1)
            else -> keywords
        }.toSet()
        // TODO fix the issue when the prefix is also a keyword (first use as a keyword and then fallback to prefix)
        val fullLinks = when {
            completeKeywords.isEmpty() ->
                fullLinkRepository.getFullLinksMatchingKeywordPrefix(keywords.last(), limit)
            else ->
                fullLinkRepository.getFullLinksContainingKeyword(completeKeywords.first())
        }.asSequence().filter { fullLink ->
            val fullLinkKeywords = fullLink.split("\\s+".toRegex()).toSet()
            val unmatchedKeywords = fullLinkKeywords - completeKeywords
            val allKeywordsMatched = unmatchedKeywords.size + completeKeywords.size == fullLinkKeywords.size
            when {
                lastKeywordIsPrefix ->
                    allKeywordsMatched && unmatchedKeywords.find { it.startsWith(keywords.last()) } != null
                else -> allKeywordsMatched
            }
        }.take(limit).toList()
        if (fullLinks.isEmpty()) {
            return emptyList()
        }
        return aliasRepository.findByIds(ids = fullLinks.map(Alias::computeId))
    }

    fun findContainingAllSearchKeywords(search: String): List<Alias> {
        val keywords = search.toKeywords()
        if (search.isBlank() || keywords.isEmpty()) {
            return aliasRepository.findAll()
        }
        val fullLinks = fullLinkRepository.getFullLinksContainingKeyword(keywords.first()).filter { fullLink ->
            fullLink.split("\\s+".toRegex()).toSet().containsAll(keywords)
        }
        if (fullLinks.isEmpty()) {
            return emptyList()
        }
        // TODO this won't work if there are more than 100 ids found => fix by introducing pagination on UI
        return aliasRepository.findByIds(ids = fullLinks.map(Alias::computeId))
    }

    fun create(alias: Alias) {
        aliasRepository.create(alias)
        fullLinkRepository.registerFullLink(
            fullLink = alias.fullLink,
            keywords = alias.fullLink.toKeywords()
        )
    }

    fun update(alias: Alias) {
        aliasRepository.update(
            findByIdOrThrow(alias.id).copy(
                redirectUrl = alias.redirectUrl,
                description = alias.description,
                ownerEmail = alias.ownerEmail
            )
        )
    }

    fun deleteById(id: String): Alias = findByIdOrThrow(id).also { alias ->
        fullLinkRepository.unregisterFullLink(
            fullLink = alias.fullLink,
            keywords = alias.fullLink.toKeywords()
        )
        aliasRepository.deleteById(id)
    }

    private fun String.toKeywords(): List<String> =
        split("\\s+".toRegex()).map(String::trim)
}