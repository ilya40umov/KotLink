package org.kotlink.domain.alias

import org.kotlink.domain.RecordNotFoundException
import java.util.concurrent.ConcurrentHashMap

class AliasService {

    private val aliases = ConcurrentHashMap<String, Alias>()

    fun findById(id: String): Alias? = aliases[id]

    fun findByIdOrThrow(id: String): Alias =
        findById(id) ?: throw RecordNotFoundException("Alias with ID '$id' not found!")

    fun findByFullLink(fullLink: String): Alias? = aliases[Alias.computeId(fullLink)]

    fun findStartingWithPrefix(fullLinkPrefix: String): List<Alias> =
        aliases.values.asSequence().filter { it.fullLink.startsWith(fullLinkPrefix) }.toList()

    fun findContainingAllSearchKeywords(search: String): List<Alias> {
        val keywords = search.split("\\s+".toRegex()).map(String::trim)
        if (search.isBlank() || keywords.isEmpty()) {
            return aliases.values.toList()
        }
        return aliases.values.asSequence().filter { alias ->
            alias.fullLink.split("\\s+".toRegex()).toSet().containsAll(keywords)
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