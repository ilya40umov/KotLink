package org.kotlink.ui.namespace

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.kotlink.ABC_NAMESPACE
import org.kotlink.TEST_ACCOUNT
import org.kotlink.WithMockMvcSetUp
import org.kotlink.core.CurrentUser
import org.kotlink.core.namespace.NamespaceService
import org.kotlink.perform
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(NamespaceUiController::class)
@WithMockMvcSetUp
class NamespaceUiControllerTest(
    @Autowired private val mvc: MockMvc
) {

    @MockBean
    private lateinit var namespaceService: NamespaceService

    @MockBean
    private lateinit var currentUser: CurrentUser

    @TestConfiguration
    class Config {
        @Bean
        fun uiValueConverter() = NamespaceUiValueConverter(mock {
            on { findByUserEmail(any()) } doReturn TEST_ACCOUNT
        })
    }

    @Test
    fun `'listNamespaces' should render a page with all namespaces`() {
        whenever(namespaceService.findAll())
            .thenReturn(listOf(ABC_NAMESPACE))

        mvc.perform(get("/ui/namespace")) {
            andExpect(status().isOk)
            andExpect(
                content()
                    .string(Matchers.containsString(ABC_NAMESPACE.keyword))
            )
        }
    }

    @Test
    fun `'newNamespace' should show the new namespace form without displaying the ID input`() {
        whenever(currentUser.getEmail()).thenReturn(TEST_ACCOUNT.email)

        mvc.perform(get("/ui/namespace/new")) {
            andExpect(status().isOk)
            andExpect(
                content()
                    .string(Matchers.containsString("""id="keyword-field""""))
            )
            andExpect(
                content()
                    .string(Matchers.not(Matchers.containsString("""id=id-field"""")))
            )
        }
    }

    @Test
    fun `'createNamespace' should display validation errors if user input is invalid`() {
        mvc.perform(
            post("/ui/namespace/new")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("keyword", "")
                .with(csrf())
        ) {
            andExpect(status().isOk)
            andExpect(
                content()
                    .string(Matchers.containsString("length must be between 1 and 128"))
            )
        }
    }

    @Test
    fun `'createNamespace' should redirect to the list of namespaces if namespace is successfully created`() {
        whenever(namespaceService.create(any()))
            .thenReturn(ABC_NAMESPACE)

        mvc.perform(
            post("/ui/namespace/new")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("keyword", "abc")
                .with(csrf())
        ) {
            andExpect(status().isFound)
        }
    }

    @Test
    fun `'createNamespace' should display the error and the user input if it couldn't create the namespace`() {
        whenever(namespaceService.create(any()))
            .thenThrow(RuntimeException("Fake exception"))

        mvc.perform(
            post("/ui/namespace/new")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("keyword", "abc")
                .with(csrf())
        ) {
            andExpect(status().isOk)
            andExpect(
                content()
                    .string(Matchers.containsString("Fake exception"))
            )
        }
    }

    @Test
    fun `'updateNamespace' should redirect to the list of namespaces and show error if namespace can't be found`() {
        mvc.perform(get("/ui/namespace/1/edit")) {
            andExpect(status().isFound)
            andExpect(
                flash()
                    .attribute<String>(
                        "error_message",
                        Matchers.containsString("Namespace #1 was not found")
                    )
            )
        }
    }

    @Test
    fun `'updateNamespace' should show the namespace info if it can be found`() {
        whenever(namespaceService.findById(any()))
            .thenReturn(ABC_NAMESPACE)

        mvc.perform(get("/ui/namespace/1/edit")) {
            andExpect(status().isOk)
            andExpect(
                content()
                    .string(Matchers.containsString(ABC_NAMESPACE.keyword))
            )
        }
    }

    @Test
    fun `'saveNamespace' should display validation errors if user input is invalid`() {
        mvc.perform(
            put("/ui/namespace/1/edit")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("id", "1")
                .param("keyword", "")
                .with(csrf())
        ) {
            andExpect(status().isOk)
            andExpect(
                content()
                    .string(Matchers.containsString("length must be between 1 and 128"))
            )
        }
    }

    @Test
    fun `'saveNamespace' should redirect to the list of namespaces if namespace is successfully updated`() {
        whenever(namespaceService.update(any()))
            .thenReturn(ABC_NAMESPACE.copy(keyword = "def"))

        mvc.perform(
            put("/ui/namespace/1/edit")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("id", "1")
                .param("keyword", "def")
                .with(csrf())
        ) {
            andExpect(status().isFound)
        }
    }

    @Test
    fun `'saveNamespace' should display the error and the user input if it couldn't update the namespace`() {
        whenever(namespaceService.update(any()))
            .thenThrow(RuntimeException("Fake exception"))

        mvc.perform(
            put("/ui/namespace/1/edit")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("id", "1")
                .param("keyword", "def")
                .with(csrf())
        ) {
            andExpect(status().isOk)
            andExpect(
                content()
                    .string(Matchers.containsString("Fake exception"))
            )
        }
    }

    @Test
    fun `'deleteNamespace' should redirect to the list view if namespace is successfully deleted`() {
        whenever(namespaceService.deleteById(any()))
            .thenReturn(ABC_NAMESPACE)

        mvc.perform(delete("/ui/namespace/1").with(csrf())) {
            andExpect(status().isFound)
            andExpect(
                flash()
                    .attribute("error_message", Matchers.isEmptyOrNullString())
            )
        }
    }

    @Test
    fun `'deleteNamespace' should display the error if it couldn't delete the namespace`() {
        whenever(namespaceService.deleteById(any()))
            .thenThrow(RuntimeException("Fake exception"))

        mvc.perform(delete("/ui/namespace/1").with(csrf())) {
            andExpect(status().isFound)
            andExpect(
                flash()
                    .attribute<String>("error_message", Matchers.containsString("Fake exception"))
            )
        }
    }
}