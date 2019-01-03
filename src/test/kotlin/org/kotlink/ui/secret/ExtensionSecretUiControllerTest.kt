package org.kotlink.ui.secret

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.kotlink.TEST_SECRET
import org.kotlink.core.secret.ApiSecretService
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
@WebMvcTest(ExtensionSecretUiController::class, secure = false)
@Import(UiTestConfig::class)
class ExtensionSecretUiControllerTest(
    @Autowired private val mvc: MockMvc
) {

    @MockBean
    private lateinit var apiSecretService: ApiSecretService

    @Test
    fun `'searchLinks' should return the page with aliases that matched the input`() {
        whenever(apiSecretService.findOrCreateForEmail(any()))
            .thenReturn(TEST_SECRET)

        mvc.perform(MockMvcRequestBuilders.get("/ui/extension_secret")) {
            andExpect(MockMvcResultMatchers.status().isOk)
            andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(TEST_SECRET.secret)))
        }
    }
}
