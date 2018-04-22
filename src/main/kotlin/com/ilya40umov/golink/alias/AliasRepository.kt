package com.ilya40umov.golink.alias

import com.ilya40umov.golink.namespace.Namespace
import org.springframework.stereotype.Repository

@Repository
class AliasRepository {

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
    }

    fun findByFullLink(fullLink: String): Alias? = aliases.find { it.fullLink == fullLink }

    fun findByFullLinkPrefix(fullLinkPrefix: String): List<Alias> =
        aliases.filter { it.fullLink.startsWith(fullLinkPrefix) }.sortedBy { it.fullLink }
}