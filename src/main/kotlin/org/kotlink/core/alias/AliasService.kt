package org.kotlink.core.alias

import org.kotlink.core.namespace.NamespaceRepo
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class AliasService(
    private val aliasRepo: AliasRepo,
    private val namespaceRepo: NamespaceRepo
) {

    fun findAll(): List<Alias> = aliasRepo.findAll()

    fun findById(id: Long): Alias? = aliasRepo.findById(id)

    @Cacheable(cacheNames = [ALIAS_BY_FULL_LINK_CACHE], unless = "#result == null")
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

    @Cacheable(cacheNames = [ALIAS_BY_FULL_LINK_PREFIX_CACHE])
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

    @Cacheable(cacheNames = [ALIAS_SEARCH_CACHE])
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
                    aliasRepo.findByNamespace(namespace.keyword) +
                    aliasRepo.findWithAtLeastOneOfTerms(terms)
            else ->
                aliasRepo.findWithAtLeastOneOfTerms(terms)
        }.toSet().toList()
    }

    @CacheEvict(allEntries = true, cacheNames = ["aliasByFullLinkPrefix", "aliasSearch"])
    fun create(alias: Alias): Alias {
        verifyFullLinkNotTaken(alias.fullLink)
        return aliasRepo.insert(alias)
    }

    @CacheEvict(allEntries = true, cacheNames = ["aliasByFullLinkPrefix", "aliasSearch"])
    fun update(alias: Alias): Alias {
        val foundAlias = aliasRepo.findByIdOrThrow(alias.id)
        if (alias.fullLink != foundAlias.fullLink) {
            verifyFullLinkNotTaken(alias.fullLink)
        }
        return aliasRepo.update(alias)
    }

    @CacheEvict(allEntries = true, cacheNames = ["aliasByFullLink", "aliasByFullLinkPrefix", "aliasSearch"])
    fun deleteById(id: Long): Alias {
        val foundAlias = aliasRepo.findByIdOrThrow(id)
        aliasRepo.deleteById(id)
        return foundAlias
    }

    private fun verifyFullLinkNotTaken(fullLink: String) {
        if (findByFullLink(fullLink) != null) {
            throw FullLinkExistsException("Link '$fullLink' is already taken")
        }
    }

    companion object {
        const val ALIAS_BY_FULL_LINK_CACHE = "aliasByFullLink"
        const val ALIAS_BY_FULL_LINK_PREFIX_CACHE = "aliasByFullLinkPrefix"
        const val ALIAS_SEARCH_CACHE = "aliasSearch"
    }
}
