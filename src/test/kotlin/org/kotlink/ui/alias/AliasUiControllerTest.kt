package org.kotlink.ui.alias

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.kotlink.ABC_NAMESPACE
import org.kotlink.INBOX_ALIAS
import org.kotlink.TEST_ACCOUNT
import org.kotlink.core.Page
import org.kotlink.core.alias.AliasService
import org.kotlink.core.namespace.NamespaceService
import org.kotlink.perform
import org.kotlink.ui.UiTestConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@ExtendWith(SpringExtension::class)
@WebMvcTest(AliasUiController::class, secure = false)
@Import(UiTestConfig::class)
class AliasUiControllerTest(
    @Autowired private val mvc: MockMvc
) {

    @MockBean
    private lateinit var aliasService: AliasService

    @MockBean
    private lateinit var namespaceService: NamespaceService

    @TestConfiguration
    class Config {
        @Bean
        fun uiValueConverter() = AliasUiValueConverter(mock {
            on { findByUserEmail(any()) } doReturn TEST_ACCOUNT
        })
    }

    @Test
    fun `'listAlias' should render a page with all aliases`() {
        whenever(aliasService.findAliasesWithFullLinkMatchingEntireInput(any(), any(), any()))
            .thenReturn(Page(records = listOf(INBOX_ALIAS), offset = 0, limit = 25, totalCount = 1))

        mvc.perform(MockMvcRequestBuilders.get("/ui/alias")) {
            andExpect(status().isOk)
            andExpect(
                content()
                    .string(Matchers.containsString(INBOX_ALIAS.link))
            )
        }
    }

    @Test
    fun `'newAlias' should show the new alias form without displaying the ID input`() {
        mvc.perform(MockMvcRequestBuilders.get("/ui/alias/new")) {
            andExpect(status().isOk)
            andExpect(
                content()
                    .string(Matchers.containsString("""id="link-field""""))
            )
            andExpect(
                content()
                    .string(Matchers.not(Matchers.containsString("""id="id-field"""")))
            )
        }
    }

    @Test
    fun `'createAlias' should display validation errors if user input is invalid`() {
        whenever(namespaceService.findById(any())).thenReturn(null)

        mvc.perform(
            MockMvcRequestBuilders.post("/ui/alias/new")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("namespaceId", "123")
                .param("link", "")
                .param("redirectUrl", "")
                .param("description", "")
        ) {
            andExpect(status().isOk)
            andExpect(
                content()
                    .string(Matchers.containsString("must not be blank"))
            )
            andExpect(
                content()
                    .string(Matchers.containsString("namespace not found"))
            )
        }
    }

    @Test
    fun `'createAlias' should redirect to the list of aliases if alias is successfully created`() {
        whenever(namespaceService.findById(any())).thenReturn(ABC_NAMESPACE)
        whenever(aliasService.create(any())).thenReturn(INBOX_ALIAS)

        mvc.perform(
            MockMvcRequestBuilders.post("/ui/alias/new")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("namespaceId", "123")
                .param("link", INBOX_ALIAS.link)
                .param("redirectUrl", INBOX_ALIAS.redirectUrl)
                .param("description", INBOX_ALIAS.description)
        ) {
            andExpect(status().isFound)
        }
    }

    @Test
    fun `'createAlias' should display the error and the user input if it couldn't create the alias`() {
        whenever(namespaceService.findById(any())).thenReturn(ABC_NAMESPACE)
        whenever(aliasService.create(any())).thenThrow(RuntimeException("Fake exception"))

        mvc.perform(
            MockMvcRequestBuilders.post("/ui/alias/new")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("namespaceId", "123")
                .param("link", INBOX_ALIAS.link)
                .param("redirectUrl", INBOX_ALIAS.redirectUrl)
                .param("description", INBOX_ALIAS.description)
        ) {
            andExpect(status().isOk)
            andExpect(
                content()
                    .string(Matchers.containsString("Fake exception"))
            )
        }
    }

    @Test
    fun `'updateAlias' should redirect to the list of aliases and show error if alias can't be found`() {
        mvc.perform(MockMvcRequestBuilders.get("/ui/alias/1/edit")) {
            andExpect(status().isFound)
            andExpect(
                flash().attribute<String>(
                    "error_message",
                    Matchers.containsString("Alias #1 was not found")
                )
            )
        }
    }

    @Test
    fun `'updateAlias' should show the alias info if it can be found`() {
        whenever(aliasService.findById(any())).thenReturn(INBOX_ALIAS)

        mvc.perform(MockMvcRequestBuilders.get("/ui/alias/1/edit")) {
            andExpect(status().isOk)
            andExpect(
                content()
                    .string(Matchers.containsString(INBOX_ALIAS.redirectUrl))
            )
        }
    }

    @Test
    fun `'saveAlias' should display validation errors if user input is invalid`() {
        whenever(namespaceService.findById(any())).thenReturn(null)

        mvc.perform(
            MockMvcRequestBuilders.put("/ui/alias/1/edit")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("id", "1")
                .param("namespaceId", "123")
                .param("link", "")
                .param("redirectUrl", "")
                .param("description", "")
        ) {
            andExpect(status().isOk)
            andExpect(
                content().string(Matchers.containsString("must not be blank"))
            )
            andExpect(
                content().string(Matchers.containsString("namespace not found"))
            )
        }
    }

    @Test
    fun `'saveAlias' should redirect to the list of aliases if alias is successfully updated`() {
        whenever(namespaceService.findById(any())).thenReturn(ABC_NAMESPACE)
        whenever(aliasService.update(any())).thenReturn(INBOX_ALIAS)

        mvc.perform(
            MockMvcRequestBuilders.put("/ui/alias/1/edit")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("id", "1")
                .param("namespaceId", "123")
                .param("link", INBOX_ALIAS.link)
                .param("redirectUrl", INBOX_ALIAS.redirectUrl)
                .param("description", INBOX_ALIAS.description)
        ) {
            andExpect(status().isFound)
        }
    }

    @Test
    fun `'saveAlias' should display the error and the user input if it couldn't update the alias`() {
        whenever(namespaceService.findById(any())).thenReturn(ABC_NAMESPACE)
        whenever(aliasService.update(any()))
            .thenThrow(RuntimeException("Fake exception"))

        mvc.perform(
            MockMvcRequestBuilders.put("/ui/alias/1/edit")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("id", "1")
                .param("namespaceId", "123")
                .param("link", INBOX_ALIAS.link)
                .param("redirectUrl", INBOX_ALIAS.redirectUrl)
                .param("description", INBOX_ALIAS.description)
        ) {
            andExpect(status().isOk)
            andExpect(
                content()
                    .string(Matchers.containsString("Fake exception"))
            )
        }
    }

    @Test
    fun `'deleteAlias' should redirect to the list view if alias is successfully deleted`() {
        whenever(aliasService.deleteById(any()))
            .thenReturn(INBOX_ALIAS)

        mvc.perform(MockMvcRequestBuilders.delete("/ui/alias/1")) {
            andExpect(status().isFound)
            andExpect(
                flash()
                    .attribute<String>("error_message", Matchers.isEmptyOrNullString())
            )
        }
    }

    @Test
    fun `'deleteAlias' should display the error if it couldn't delete the alias`() {
        whenever(aliasService.deleteById(any()))
            .thenThrow(RuntimeException("Fake exception"))

        mvc.perform(MockMvcRequestBuilders.delete("/ui/alias/1")) {
            andExpect(status().isFound)
            andExpect(
                flash()
                    .attribute<String>("error_message", Matchers.containsString("Fake exception"))
            )
        }
    }
}