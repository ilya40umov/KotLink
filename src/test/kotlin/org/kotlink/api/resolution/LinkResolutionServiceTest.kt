package org.kotlink.api.resolution

import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.whenever
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.kotlink.INBOX_ALIAS
import org.kotlink.core.alias.AliasService
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class LinkResolutionServiceTest(
    @Mock private val aliasService: AliasService
) {

    private val kotLinkService = LinkResolutionService(aliasService)

    @Test
    fun `'findRedirectUrlByLink' should return redirect url if link is found`() {
        whenever(aliasService.findByFullLink(eq(INBOX_ALIAS.link)))
            .thenReturn(INBOX_ALIAS)

        kotLinkService.findRedirectUrlByLink(
            userProvidedLink = INBOX_ALIAS.link
        ).also {
            it shouldBeEqualTo INBOX_ALIAS.redirectUrl
        }
    }

    @Test
    fun `'findRedirectUrlByLink' should return no url if link is not found`() {
        kotLinkService.findRedirectUrlByLink(
            userProvidedLink = INBOX_ALIAS.link
        ).also {
            it shouldBe null
        }
    }

    @Test
    fun `'suggestAliasesByLinkPrefix' should return suggestions created from matching aliases`() {
        whenever(aliasService.findByFullLinkPrefix(eq("in")))
            .thenReturn(listOf(INBOX_ALIAS))

        kotLinkService.suggestAliasesByLinkPrefix(
            userProvidedLinkPrefix = "in",
            redirectUri = "http://localhost:8080/api/link/redirect"
        ).also {
            it shouldBeEqualTo OpenSearchSuggestions(
                prefix = "in",
                links = listOf(INBOX_ALIAS.fullLink),
                descriptions = listOf(INBOX_ALIAS.fullLink),
                redirectUrls = listOf(
                    "http://localhost:8080/api/link/redirect?link=${INBOX_ALIAS.fullLink}"
                        .replace(" ", "%20")
                )
            )
        }
    }
}