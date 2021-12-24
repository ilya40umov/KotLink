package org.kotlink.domain.alias

import java.util.concurrent.ConcurrentHashMap

class AliasService {

    private val aliases = ConcurrentHashMap<String, Alias>()

    fun findById(id: String): Alias? = aliases[id]

    fun findByFullLink(fullLink: String): Alias? = aliases[Alias.computeId(fullLink)]

    fun findByFullLinkPrefix(fullLinkPrefix: String): List<Alias> =
        aliases.values.asSequence().filter { it.fullLink.startsWith(fullLinkPrefix) }.toList()

    fun findAliasesWithFullLinkMatchingEntireInput(
        userProvidedInput: String
    ): List<Alias> {
        val keywords = userProvidedInput.split("\\s+")
        if (userProvidedInput.isBlank() || keywords.isEmpty()) {
            return aliases.values.toList()
        }
        return aliases.values.asSequence().filter { alias ->
            alias.fullLink.split("\\s+").toSet().containsAll(keywords)
        }.toList()
    }

    fun create(alias: Alias) {
        aliases += alias.id to alias
    }

    fun update(alias: Alias) {
        if (aliases.containsKey(alias.id)) {
            aliases += alias.id to alias
        }
    }

    fun deleteById(id: String): Alias {
        return aliases.remove(id) ?: throw IllegalArgumentException("Alias not found!")
    }
}