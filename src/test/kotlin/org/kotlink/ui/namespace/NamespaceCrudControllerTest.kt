package org.kotlink.ui.namespace

import com.nhaarman.mockitokotlin2.whenever
import org.hamcrest.Matchers
import org.junit.Test
import org.junit.runner.RunWith
import org.kotlink.ABC_NAMESPACE
import org.kotlink.INBOX_ALIAS
import org.kotlink.api.namespace.NamespaceRepo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@RunWith(SpringRunner::class)
@WebMvcTest(NamespaceCrudController::class)
class NamespaceCrudControllerTest {

    @Autowired
    private lateinit var mvc: MockMvc

    @MockBean
    private lateinit var namespaceRepo: NamespaceRepo

    @Test
    fun `'listNamespaces' should return the page with all namespaces`() {
        whenever(namespaceRepo.findAll()).thenReturn(listOf(ABC_NAMESPACE))

        mvc.perform(MockMvcRequestBuilders.get("/ui/search?input=inbox"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(INBOX_ALIAS.fullLink)))
    }
}