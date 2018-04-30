package com.ilya40umov.golink.namespace

import com.ilya40umov.golink.ABC_NAMESPACE
import com.ilya40umov.golink.ABC_NAMESPACE_ID
import com.ilya40umov.golink.DEFAULT_NAMESPACE
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.hamcrest.Matchers
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@RunWith(SpringRunner::class)
@WebMvcTest(NamespaceController::class)
class NamespaceControllerTest {

    @Autowired
    private lateinit var mvc: MockMvc

    @MockBean
    private lateinit var namespaceRepo: NamespaceRepo

    @Test
    fun `findAll returns all namespaces from repository`() {
        whenever(namespaceRepo.findAll()).thenReturn(listOf(DEFAULT_NAMESPACE, ABC_NAMESPACE))

        mvc.perform(get("/namespace"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$[0].keyword").value(DEFAULT_NAMESPACE.keyword))
            .andExpect(jsonPath("$[1].keyword").value(ABC_NAMESPACE.keyword))
    }

    @Test
    fun `findAll returns no namespaces if repository is empty`() {
        whenever(namespaceRepo.findAll()).thenReturn(listOf())

        mvc.perform(get("/namespace"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isEmpty)
    }

    @Test
    fun `findById returns corresponding namespace if specified ID exists`() {
        whenever(namespaceRepo.findById(ABC_NAMESPACE_ID)).thenReturn(ABC_NAMESPACE)

        mvc.perform(get("/namespace/$ABC_NAMESPACE_ID"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.keyword").value(ABC_NAMESPACE.keyword))
    }

    @Test
    fun `findById returns 404 if specified ID does not exist`() {
        mvc.perform(get("/namespace/12345"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `create returns 201 and location header if namespace is created successfully`() {
        whenever(namespaceRepo.insert(any())).thenReturn(ABC_NAMESPACE)

        mvc.perform(
            post("/namespace")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"keyword":"abc"}"""))
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", Matchers.endsWith("/namespace/$ABC_NAMESPACE_ID")))
    }

    @Test
    fun `create returns 409 if namespace matching this keyword already exists`() {
        whenever(namespaceRepo.findByKeyword(ABC_NAMESPACE.keyword)).thenReturn(ABC_NAMESPACE)

        mvc.perform(
            post("/namespace")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"keyword":"abc"}"""))
            .andExpect(status().isConflict)
    }

    @Test
    fun `delete returns 204 and removes namespace if specified ID exists`() {
        whenever(namespaceRepo.findById(ABC_NAMESPACE_ID)).thenReturn(ABC_NAMESPACE)

        mvc.perform(delete("/namespace/$ABC_NAMESPACE_ID"))
            .andExpect(status().isNoContent)

        verify(namespaceRepo).deleteById(ABC_NAMESPACE_ID)
    }

    @Test
    fun `delete returns 400 if specified ID does not exist`() {
        mvc.perform(delete("/namespace/12345"))
            .andExpect(status().isBadRequest)

        verify(namespaceRepo, times(0)).deleteById(any())
    }
}