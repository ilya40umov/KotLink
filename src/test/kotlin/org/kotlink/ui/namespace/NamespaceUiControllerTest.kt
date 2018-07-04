package org.kotlink.ui.namespace

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import org.hamcrest.Matchers
import org.junit.Test
import org.junit.runner.RunWith
import org.kotlink.ABC_NAMESPACE
import org.kotlink.core.namespace.NamespaceService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@RunWith(SpringRunner::class)
@WebMvcTest(NamespaceUiController::class, secure = false)
class NamespaceUiControllerTest {

    @Autowired
    private lateinit var mvc: MockMvc

    @MockBean
    private lateinit var namespaceService: NamespaceService

    @Test
    fun `'listNamespaces' should render a page with all namespaces`() {
        whenever(namespaceService.findAll())
            .thenReturn(listOf(ABC_NAMESPACE))

        mvc.perform(get("/ui/namespace"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content()
                .string(Matchers.containsString(ABC_NAMESPACE.keyword)))
    }

    @Test
    fun `'newNamespace' should show the new namespace form without displaying the ID input`() {
        mvc.perform(get("/ui/namespace/new"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content()
                .string(Matchers.containsString("""id="keyword-field"""")))
            .andExpect(MockMvcResultMatchers.content()
                .string(Matchers.not(Matchers.containsString("""id=id-field""""))))
    }

    @Test
    fun `'createNamespace' should display validation errors if user input is invalid`() {
        mvc.perform(post("/ui/namespace/new")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("keyword", ""))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content()
                .string(Matchers.containsString("length must be between 1 and 128")))
    }

    @Test
    fun `'createNamespace' should redirect to the list of namespaces if namespace is successfully created`() {
        whenever(namespaceService.create(any()))
            .thenReturn(ABC_NAMESPACE)

        mvc.perform(post("/ui/namespace/new")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("keyword", "abc"))
            .andExpect(MockMvcResultMatchers.status().isFound)
    }

    @Test
    fun `'createNamespace' should display the error and the user input if it couldn't create the namespace`() {
        whenever(namespaceService.create(any()))
            .thenThrow(RuntimeException("Fake exception"))

        mvc.perform(post("/ui/namespace/new")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("keyword", "abc"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content()
                .string(Matchers.containsString("Fake exception")))
    }

    @Test
    fun `'updateNamespace' should redirect to the list of namespaces and show error if namespace can't be found`() {
        mvc.perform(get("/ui/namespace/1/edit"))
            .andExpect(MockMvcResultMatchers.status().isFound)
            .andExpect(MockMvcResultMatchers.flash()
                .attribute<String>("error_message",
                    Matchers.containsString("Namespace #1 was not found")))
    }

    @Test
    fun `'updateNamespace' should show the namespace info if it can be found`() {
        whenever(namespaceService.findById(any()))
            .thenReturn(ABC_NAMESPACE)

        mvc.perform(get("/ui/namespace/1/edit"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content()
                .string(Matchers.containsString(ABC_NAMESPACE.keyword)))
    }

    @Test
    fun `'saveNamespace' should display validation errors if user input is invalid`() {
        mvc.perform(put("/ui/namespace/1/edit")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("id", "1")
            .param("keyword", ""))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content()
                .string(Matchers.containsString("length must be between 1 and 128")))
    }

    @Test
    fun `'saveNamespace' should redirect to the list of namespaces if namespace is successfully updated`() {
        whenever(namespaceService.update(any()))
            .thenReturn(ABC_NAMESPACE.copy(keyword = "def"))

        mvc.perform(put("/ui/namespace/1/edit")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("id", "1")
            .param("keyword", "def"))
            .andExpect(MockMvcResultMatchers.status().isFound)
    }

    @Test
    fun `'saveNamespace' should display the error and the user input if it couldn't update the namespace`() {
        whenever(namespaceService.update(any()))
            .thenThrow(RuntimeException("Fake exception"))

        mvc.perform(put("/ui/namespace/1/edit")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("id", "1")
            .param("keyword", "def"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content()
                .string(Matchers.containsString("Fake exception")))
    }

    @Test
    fun `'deleteNamespace' should redirect to the list view if namespace is successfully deleted`() {
        whenever(namespaceService.deleteById(any()))
            .thenReturn(true)

        mvc.perform(delete("/ui/namespace/1"))
            .andExpect(MockMvcResultMatchers.status().isFound)
            .andExpect(MockMvcResultMatchers.flash()
                .attribute<String>("error_message", Matchers.isEmptyOrNullString()))
    }

    @Test
    fun `'deleteNamespace' should display the error if it couldn't delete the namespace`() {
        whenever(namespaceService.deleteById(any()))
            .thenThrow(RuntimeException("Fake exception"))

        mvc.perform(delete("/ui/namespace/1"))
            .andExpect(MockMvcResultMatchers.status().isFound)
            .andExpect(MockMvcResultMatchers.flash()
                .attribute<String>("error_message", Matchers.containsString("Fake exception")))
    }
}