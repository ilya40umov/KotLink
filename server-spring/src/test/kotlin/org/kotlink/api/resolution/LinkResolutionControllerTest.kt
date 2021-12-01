package org.kotlink.api.resolution

import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Test
import org.kotlink.INBOX_ALIAS
import org.kotlink.INIT_ALIAS
import org.kotlink.WithMockMvcSetUp
import org.kotlink.perform
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(LinkResolutionController::class)
@WithMockMvcSetUp
class LinkResolutionControllerTest(
    @Autowired private val mvc: MockMvc
) {

    @MockBean
    private lateinit var linkResolutionService: LinkResolutionService

    @Test
    fun `'redirectByAlias' should redirect to link's URL if alias exists`() {
        whenever(linkResolutionService.findRedirectUrlByLink("inbox"))
            .thenReturn("https://inbox.google.com")

        mvc.perform(get("/api/link/redirect?link=inbox")) {
            andExpect(status().isFound)
            andExpect(redirectedUrl("https://inbox.google.com"))
        }
    }

    @Test
    fun `'redirectByAlias' should redirect to search endpoint if alias does not exist`() {
        mvc.perform(get("/api/link/redirect?link=abc")) {
            andExpect(status().isFound)
            andExpect(redirectedUrl("/ui/search?input=abc"))
        }
    }

    @Test
    fun `'suggestAliases' should return suggestions in opensearch format if mode is opensearch`() {
        whenever(
            linkResolutionService.suggestAliasesByLinkPrefix(
                userProvidedLinkPrefix = "in",
                redirectUri = "http://localhost/api/link/redirect"
            )
        ).thenReturn(
            OpenSearchSuggestions(
                prefix = "in",
                redirectUri = "http://localhost/api/link/redirect",
                aliases = listOf(INBOX_ALIAS, INIT_ALIAS)
            )
        )

        mvc.perform(get("/api/link/suggest?link=in&mode=opensearch")) {
            andExpect(status().isOk)
            andExpect(jsonPath("$").isArray)
            andExpect(jsonPath("$[0]").value("in"))
            andExpect(jsonPath("$[1]").isArray)
            andExpect(jsonPath("$[1][0]").value(INBOX_ALIAS.fullLink))
            andExpect(jsonPath("$[1][1]").value(INIT_ALIAS.fullLink))
            andExpect(jsonPath("$[2]").isArray)
            andExpect(jsonPath("$[2][0]").value(INBOX_ALIAS.description))
            andExpect(jsonPath("$[3]").isArray)
            andExpect(
                jsonPath("$[3][0]")
                    .value(
                        "http://localhost/api/link/redirect?link=${INBOX_ALIAS.fullLink}"
                            .replace(" ", "%20")
                    )
            )
        }
    }

    @Test
    fun `'suggestAliases' should return suggestions in simple format if mode is not present`() {
        whenever(
            linkResolutionService.suggestAliasesByLinkPrefix(
                userProvidedLinkPrefix = "in",
                redirectUri = "http://localhost/api/link/redirect"
            )
        ).thenReturn(
            OpenSearchSuggestions(
                prefix = "in",
                redirectUri = "http://localhost:8080",
                aliases = listOf(INBOX_ALIAS, INIT_ALIAS)
            )
        )

        mvc.perform(get("/api/link/suggest?link=in&mode=simple")) {
            andExpect(status().isOk)
            andExpect(jsonPath("$").isArray)
            andExpect(jsonPath("$[0].first").value(INBOX_ALIAS.fullLink))
            andExpect(jsonPath("$[0].second").value(INBOX_ALIAS.description))
            andExpect(jsonPath("$[1].first").value(INIT_ALIAS.fullLink))
            andExpect(jsonPath("$[1].second").isEmpty)
        }
    }
}
