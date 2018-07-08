package org.kotlink.core.alias

import org.kotlink.core.alias.AliasService.Companion.ALIAS_CACHE_NAME
import org.kotlink.core.namespace.NamespaceRepo
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
@CacheConfig(cacheNames = [ALIAS_CACHE_NAME])
class AliasService(
    private val aliasRepo: AliasRepo,
    private val namespaceRepo: NamespaceRepo
) {

    fun findAll(): List<Alias> = aliasRepo.findAll()

    fun findById(id: Long): Alias? = aliasRepo.findById(id)

    fun findByFullLink(fullLink: String): Alias? {
        return aliasRepo.findByNamespaceAndLink(namespace = "", link = fullLink)
            .let { alias ->
                when {
                    alias != null -> alias
                    fullLink.contains(" ") ->
                        fullLink.split(" ", limit = 2).let { terms ->
                            aliasRepo.findByNamespaceAndLink(terms[0], terms[1])
                        }
                    else -> null
                }
            }
    }

    @Cacheable
    fun findByFullLinkPrefix(fullLinkPrefix: String): List<Alias> {
        val matchesInDefaultNamespace =
            aliasRepo.findByNamespaceAndLinkPrefix(namespace = "", linkPrefix = fullLinkPrefix)
        return when {
            fullLinkPrefix.contains(" ") ->
                fullLinkPrefix.split(" ", limit = 2).let {
                    matchesInDefaultNamespace + aliasRepo.findByNamespaceAndLinkPrefix(it[0], it[1])
                }
            else -> matchesInDefaultNamespace + aliasRepo.findByNamespacePrefix(fullLinkPrefix)
        }
    }

    @Cacheable
    fun searchAliasesMatchingInput(userProvidedInput: String): List<Alias> {
        val terms = userProvidedInput
            .replace("[^A-Za-z0-9\\s+]".toRegex(), "")
            .toLowerCase()
            .split("\\s+".toRegex())
            .filter { it.isNotBlank() }
        if (terms.isEmpty()) {
            return emptyList()
        }
        val namespace = namespaceRepo.findByKeyword(terms.first())
        return when {
            namespace != null && terms.size == 1 ->
                aliasRepo.findByNamespace(namespace.keyword) +
                    aliasRepo.findWithAtLeastOneOfTerms(terms)
            namespace != null ->
                aliasRepo.findByNamespaceAndWithAtLeastOneOfTerms(namespace.keyword, terms.subList(1, terms.size)) +
                    aliasRepo.findWithAtLeastOneOfTerms(terms)
            else ->
                aliasRepo.findWithAtLeastOneOfTerms(terms)
        }.toSet().toList()
    }

    @CacheEvict(allEntries = true)
    fun create(alias: Alias): Alias = aliasRepo.insert(alias)

    @CacheEvict(allEntries = true)
    fun update(alias: Alias): Alias = aliasRepo.update(alias)

    @CacheEvict(allEntries = true)
    fun deleteById(id: Long): Boolean = aliasRepo.deleteById(id)

    companion object {
        const val ALIAS_CACHE_NAME = "aliases"
    }
}
