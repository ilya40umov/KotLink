package org.kotlink.api.resolution

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldEqual
import org.junit.Test
import org.junit.runner.RunWith
import org.kotlink.INBOX_ALIAS
import org.kotlink.core.alias.AliasService
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class LinkResolutionServiceTest {

    private val aliasService = mock<AliasService> {}
    private val kotLinkService = LinkResolutionService(aliasService)

    @Test
    fun `'findRedirectUrlByLink' should return redirect url if link is found`() {
        whenever(aliasService.findByFullLink(eq(INBOX_ALIAS.link))).doReturn(INBOX_ALIAS)

        val redirectUrl = kotLinkService.findRedirectUrlByLink(INBOX_ALIAS.link)

        redirectUrl shouldEqual INBOX_ALIAS.redirectUrl
    }

    @Test
    fun `'findRedirectUrlByLink' should return no url if link is not found`() {
        val redirectUrl = kotLinkService.findRedirectUrlByLink(INBOX_ALIAS.link)

        redirectUrl.shouldBeNull()
    }

    @Test
    fun `'suggestAliasesByLinkPrefix' should return suggestions created from matching aliases`() {
        whenever(aliasService.findByFullLinkPrefix("in"))
            .thenReturn(listOf(INBOX_ALIAS))

        val suggestions = kotLinkService
            .suggestAliasesByLinkPrefix("in", "http://localhost:8080/api/link/redirect")

        suggestions shouldEqual OpenSearchSuggestions(
            prefix = "in",
            links = listOf(INBOX_ALIAS.fullLink),
            descriptions = listOf(INBOX_ALIAS.fullLink),
            redirectUrls = listOf("http://localhost:8080/api/link/redirect?link=${INBOX_ALIAS.fullLink}"
                .replace(" ", "%20"))
        )
    }
}