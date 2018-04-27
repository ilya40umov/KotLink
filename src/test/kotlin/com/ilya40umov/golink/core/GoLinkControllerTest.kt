package com.ilya40umov.golink.core

import com.ilya40umov.golink.INBOX_ALIAS
import com.ilya40umov.golink.INIT_ALIAS
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath

@RunWith(SpringRunner::class)
@WebMvcTest(GoLinkController::class)
class GoLinkControllerTest {

    @Autowired
    private lateinit var mvc: MockMvc

    @MockBean
    private lateinit var goLinkService: GoLinkService

    @Test
    fun `redirectByAlias should redirect to link's URL if alias exists`() {
        whenever(goLinkService.findRedirectUrlByLink("inbox"))
            .thenReturn("https://inbox.google.com")

        mvc.perform(get("/go/redirect?link=inbox"))
            .andExpect(status().isFound)
            .andExpect(redirectedUrl("https://inbox.google.com"))
    }

    @Test
    fun `redirectByAlias should redirect to search endpoint if alias does not exist`() {
        mvc.perform(get("/go/redirect?link=abc"))
            .andExpect(status().isFound)
            .andExpect(redirectedUrl("/go/search?input=abc"))
    }

    @Test
    fun `suggestAliases should return suggestions in opensearch format if mode is opensearch`() {
        whenever(goLinkService.suggestAliasesByLinkPrefix("in"))
            .thenReturn(OpenSearchSuggestions(prefix = "in", aliases = listOf(INBOX_ALIAS, INIT_ALIAS)))

        mvc.perform(get("/go/suggest?link=in&mode=opensearch"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$[0]").value("in"))
            .andExpect(jsonPath("$[1]").isArray)
            .andExpect(jsonPath("$[1][0]").value(INBOX_ALIAS.fullLink))
            .andExpect(jsonPath("$[1][1]").value(INIT_ALIAS.fullLink))
            .andExpect(jsonPath("$[2]").isArray)
            .andExpect(jsonPath("$[2][0]").value(INBOX_ALIAS.fullLink))
            .andExpect(jsonPath("$[3]").isArray)
            .andExpect(jsonPath("$[3][0]").value(INBOX_ALIAS.redirectUrl))
    }

    @Test
    fun `suggestAliases should return suggestions in simple format if mode is not present`() {
        whenever(goLinkService.suggestAliasesByLinkPrefix("in"))
            .thenReturn(OpenSearchSuggestions(prefix = "in", aliases = listOf(INBOX_ALIAS, INIT_ALIAS)))

        mvc.perform(get("/go/suggest?link=in&mode=simple"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$[0]").value(INBOX_ALIAS.fullLink))
            .andExpect(jsonPath("$[1]").value(INIT_ALIAS.fullLink))
    }

    @Test
    fun `searchLinks should return the aliases that matched the input`() {
        whenever(goLinkService.searchAliasesMatchingInput("inbox"))
            .thenReturn(listOf(INBOX_ALIAS))

        mvc.perform(get("/go/search?input=inbox"))
            .andExpect(status().isOk)
            .andExpect(xpath("""/html/body/ul[@id="found-aliases"]/li[1]/a[text()]""")
                .string(INBOX_ALIAS.fullLink))
    }
}