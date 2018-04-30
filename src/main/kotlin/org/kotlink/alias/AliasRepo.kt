package org.kotlink.alias

import org.kotlink.namespace.Namespace
import org.springframework.stereotype.Repository

@Repository
class AliasRepo {

    private val defaultNamespace = Namespace(id = 1, keyword = "")
    private val aliases = mutableListOf<Alias>().apply {
        add(
            Alias(
                id = 1,
                namespace = defaultNamespace,
                link = "inbox",
                redirectUrl = "https://inbox.google.com/")
        )
        add(
            Alias(
                id = 2,
                namespace = defaultNamespace,
                link = "gmail",
                redirectUrl = "https://mail.google.com/")
        )
        add(
            Alias(
                id = 3,
                namespace = defaultNamespace,
                link = "init",
                redirectUrl = "https://en.wikipedia.org/wiki/Systemd")
        )
        add(
            Alias(
                id = 4,
                namespace = defaultNamespace,
                link = "inbox shortcuts",
                redirectUrl = "https://support.google.com/a/users/answer/163225?hl=en")
        )
    }

    fun findByFullLink(fullLink: String): Alias? = aliases.find { it.fullLink == fullLink }

    fun findByFullLinkPrefix(fullLinkPrefix: String): List<Alias> =
        aliases.filter { it.fullLink.startsWith(fullLinkPrefix) }.sortedBy { it.fullLink }

    fun findWithAtLeastOneOfKeywords(keywords: List<String>): List<Alias> {
        val keywordsSet = keywords.toSet()
        val keywordCountByAlias =
            aliases.associate { it to it.fullLink.split(" ").count(keywordsSet::contains) }
        return aliases
            .filter { keywordCountByAlias[it]!! > 0 }
            .sortedByDescending { keywordCountByAlias[it]!! }
    }
}