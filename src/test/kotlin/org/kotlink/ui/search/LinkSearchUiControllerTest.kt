package org.kotlink.ui.search

import com.nhaarman.mockitokotlin2.whenever
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.kotlink.INBOX_ALIAS
import org.kotlink.core.alias.AliasService
import org.kotlink.perform
import org.kotlink.ui.UiTestConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@ExtendWith(SpringExtension::class)
@WebMvcTest(LinkSearchUiController::class, secure = false)
@Import(UiTestConfig::class)
class LinkSearchUiControllerTest(
    @Autowired private val mvc: MockMvc
) {

    @MockBean
    private lateinit var aliasService: AliasService

    @Test
    fun `'searchLinks' should return the page with aliases that matched the input`() {
        whenever(aliasService.searchAliasesMatchingAtLeastPartOfInput("inbox"))
            .thenReturn(listOf(INBOX_ALIAS))

        mvc.perform(MockMvcRequestBuilders.get("/ui/search?input=inbox")) {
            andExpect(MockMvcResultMatchers.status().isOk)
            andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(INBOX_ALIAS.fullLink)))
        }
    }
}
