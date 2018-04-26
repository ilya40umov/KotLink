package com.ilya40umov.golink.core

import com.ilya40umov.golink.INBOX_ALIAS
import com.ilya40umov.golink.alias.AliasRepository
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldEqual
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class GoLinkServiceTest {

    private val aliasRepository = mock<AliasRepository> {}
    private val goLinkService = GoLinkService(aliasRepository)

    @Test
    fun `findRedirectUrlByLink should return redirect url if link is found`() {
        whenever(aliasRepository.findByFullLink(eq(INBOX_ALIAS.link))).doReturn(INBOX_ALIAS)

        val redirectUrl = goLinkService.findRedirectUrlByLink(INBOX_ALIAS.link)

        redirectUrl shouldEqual INBOX_ALIAS.redirectUrl
    }

    @Test
    fun `findRedirectUrlByLink should return no url if link is not found`() {
        val redirectUrl = goLinkService.findRedirectUrlByLink(INBOX_ALIAS.link)

        redirectUrl.shouldBeNull()
    }

    @Test
    fun `suggestAliasesByLinkPrefix should return suggestions created from matching aliases`() {
        whenever(aliasRepository.findByFullLinkPrefix("in"))
            .thenReturn(listOf(INBOX_ALIAS))

        val suggestions = goLinkService.suggestAliasesByLinkPrefix("in")

        suggestions shouldEqual OpenSearchSuggestions(
            prefix = "in",
            links = listOf(INBOX_ALIAS.fullLink),
            descriptions = listOf(INBOX_ALIAS.fullLink),
            redirectUrls = listOf(INBOX_ALIAS.redirectUrl)
        )
    }

    @Test
    fun `searchAliasesMatchingInput split keywords in user input and return found aliases`() {
        whenever(aliasRepository.findWithAtLeastOneOfKeywords(listOf("inbox", "gmail")))
            .thenReturn(listOf(INBOX_ALIAS))

        val aliases = goLinkService.searchAliasesMatchingInput("inbox gmail")

        aliases shouldEqual listOf(INBOX_ALIAS)
    }
}

