package org.kotlink.core.alias

import org.kotlink.core.CurrentUser
import org.kotlink.core.OperationDeniedException
import org.kotlink.core.Page
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

    fun findById(id: Long): Alias? = aliasRepo.findById(id)

    @Cacheable(cacheNames = [ALIAS_BY_FULL_LINK_CACHE], unless = "#result == null")
    fun findByFullLink(fullLink: String): Alias? {
        val trimmedFullLink = fullLink.trim().lowercase()
        return aliasRepo.findByFullLink(trimmedFullLink) ?: run {
            val matchingAliases = findMatchingAllTermsInFullLinkSortedByCommonPrefix(
                fullLink = trimmedFullLink,
                isPrefix = false
            )
            matchingAliases.firstOrNull()?.takeIf {
                it.fullLink.length == trimmedFullLink.length
            }
        }
    }

    @Cacheable(cacheNames = [ALIAS_BY_FULL_LINK_PREFIX_CACHE])
    fun findByFullLinkPrefix(fullLinkPrefix: String): List<Alias> {
        val prefixTrimmed = fullLinkPrefix.trim()
        if (prefixTrimmed.isBlank()) {
            return emptyList()
        }
        return findMatchingAllTermsInFullLinkSortedByCommonPrefix(
            fullLink = prefixTrimmed,
            isPrefix = !fullLinkPrefix.last().isWhitespace()
        )
    }

    fun searchAliasesMatchingAtLeastPartOfInput(userProvidedInput: String): List<Alias> {
        val terms = extractTerms(userProvidedInput)
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

    fun findAliasesWithFullLinkMatchingEntireInput(
        userProvidedInput: String,
        offset: Int,
        limit: Int
    ): Page<Alias> {
        val terms = extractTerms(userProvidedInput)
        if (terms.isEmpty()) {
            return Page(
                records = aliasRepo.findAll(offset, limit),
                offset = offset,
                limit = limit,
                totalCount = aliasRepo.countAll()
            )
        }
        return Page(
            records = aliasRepo.findWithAllOfTermsInFullLink(terms, false, offset, limit),
            offset = offset,
            limit = limit,
            totalCount = aliasRepo.countWithAllOfTermsInFullLink(terms)
        )
    }

    @EditOp
    @CacheEvict(
        allEntries = true,
        cacheNames = [ALIAS_BY_FULL_LINK_CACHE, ALIAS_BY_FULL_LINK_PREFIX_CACHE]
    )
    fun create(alias: Alias): Alias {
        verifyFullLinkNotTaken(alias.fullLink)
        return aliasRepo.insert(alias)
    }

    @EditOp
    @CacheEvict(
        allEntries = true,
        cacheNames = [ALIAS_BY_FULL_LINK_CACHE, ALIAS_BY_FULL_LINK_PREFIX_CACHE]
    )
    fun update(alias: Alias): Alias {
        val foundAlias = aliasRepo.findByIdOrThrow(alias.id)
        if (alias.fullLink != foundAlias.fullLink) {
            verifyFullLinkNotTaken(alias.fullLink)
        }
        foundAlias.verifyCanModify(currentUser) {
            "Only the link owner (${foundAlias.ownerAccount.email}) " +
                    "or the namespace owner (${foundAlias.namespace.ownerAccount.email}) can modify the link"
        }
        return aliasRepo.update(alias)
    }

    @EditOp
    @CacheEvict(
        allEntries = true,
        cacheNames = [ALIAS_BY_FULL_LINK_CACHE, ALIAS_BY_FULL_LINK_PREFIX_CACHE]
    )
    fun deleteById(id: Long): Alias {
        val foundAlias = aliasRepo.findByIdOrThrow(id)
        foundAlias.verifyCanModify(currentUser) {
            "Only the link owner (${foundAlias.ownerAccount.email}) " +
                    "or the namespace owner (${foundAlias.namespace.ownerAccount.email}) can remove the link"
        }
        aliasRepo.deleteById(id)
        return foundAlias
    }

    private fun Alias.verifyCanModify(currentUser: CurrentUser, message: () -> String) {
        val isOwner = currentUser.getAccount().id in setOf(
            ownerAccount.id,
            namespace.ownerAccount.id
        )
        if (!isOwner && !currentUser.isAdmin()) {
            throw OperationDeniedException(message = message())
        }
    }

    private fun findMatchingAllTermsInFullLinkSortedByCommonPrefix(
        fullLink: String,
        isPrefix: Boolean
    ): List<Alias> {
        return aliasRepo.findWithAllOfTermsInFullLink(
            terms = extractTerms(fullLink),
            lastTermIsPrefix = isPrefix,
            offset = 0,
            limit = Int.MAX_VALUE
        ).map {
            it to it.fullLink.commonPrefixWith(fullLink, ignoreCase = true)
        }.sortedByDescending {
            it.second.length
        }.map {
            it.first
        }
    }

    private fun extractTerms(userProvidedInput: String) =
        userProvidedInput
            .replace("[^A-Za-z0-9\\s+]".toRegex(), "")
            .lowercase()
            .split("\\s+".toRegex())
            .filter { it.isNotBlank() }

    private fun verifyFullLinkNotTaken(fullLink: String) {
        if (aliasRepo.findByFullLink(fullLink) != null) {
            throw FullLinkExistsException("Link '$fullLink' is already taken")
        }
    }

    companion object {
        const val ALIAS_BY_FULL_LINK_CACHE = "aliasByFullLink"
        const val ALIAS_BY_FULL_LINK_PREFIX_CACHE = "aliasByFullLinkPrefix"
    }
}
