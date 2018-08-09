package org.kotlink.core.alias

import org.kotlink.core.CurrentUser
import org.kotlink.core.OperationDeniedException
import org.kotlink.core.ipblock.EditOp
import org.kotlink.core.namespace.NamespaceRepo
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class AliasService(
    private val aliasRepo: AliasRepo,
    private val namespaceRepo: NamespaceRepo,
    private val currentUser: CurrentUser
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

    @EditOp
    @CacheEvict(
        allEntries = true,
        cacheNames = [ALIAS_BY_FULL_LINK_CACHE, ALIAS_BY_FULL_LINK_PREFIX_CACHE, ALIAS_SEARCH_CACHE]
    )
    fun create(alias: Alias): Alias {
        verifyFullLinkNotTaken(alias.fullLink)
        return aliasRepo.insert(alias)
    }

    @EditOp
    @CacheEvict(
        allEntries = true,
        cacheNames = [ALIAS_BY_FULL_LINK_CACHE, ALIAS_BY_FULL_LINK_PREFIX_CACHE, ALIAS_SEARCH_CACHE]
    )
    fun update(alias: Alias): Alias {
        val foundAlias = aliasRepo.findByIdOrThrow(alias.id)
        if (alias.fullLink != foundAlias.fullLink) {
            verifyFullLinkNotTaken(alias.fullLink)
        }
        if (currentUser.getAccount().id !in setOf(
                foundAlias.ownerAccount.id,
                foundAlias.namespace.ownerAccount.id)) {
            throw OperationDeniedException(
                "Only the link owner (${foundAlias.ownerAccount.email}) " +
                    "or the namespace owner (${foundAlias.namespace.ownerAccount.email}) can modify the link")
        }
        return aliasRepo.update(alias)
    }

    @EditOp
    @CacheEvict(
        allEntries = true,
        cacheNames = [ALIAS_BY_FULL_LINK_CACHE, ALIAS_BY_FULL_LINK_PREFIX_CACHE, ALIAS_SEARCH_CACHE]
    )
    fun deleteById(id: Long): Alias {
        val foundAlias = aliasRepo.findByIdOrThrow(id)
        if (currentUser.getAccount().id !in setOf(
                foundAlias.ownerAccount.id,
                foundAlias.namespace.ownerAccount.id)) {
            throw OperationDeniedException(
                "Only the link owner (${foundAlias.ownerAccount.email}) " +
                    "or the namespace owner (${foundAlias.namespace.ownerAccount.email}) can remove the link")
        }
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
