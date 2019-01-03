package org.kotlink.ui

import com.nhaarman.mockitokotlin2.whenever
import org.amshove.kluent.shouldEqual
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import javax.servlet.http.HttpServletRequest

@ExtendWith(MockitoExtension::class)
class ViewUtilsTest {

    private val viewUtils = ViewUtils()

    @Test
    fun `'serverUrlFromRequest' should only include port into URL if it's not standard`(
        @Mock request: HttpServletRequest
    ) {
        whenever(request.serverPort).thenReturn(8080)
        whenever(request.scheme).thenReturn("http")
        whenever(request.serverName).thenReturn("localhost")

        viewUtils.serverUrlFromRequest(request) shouldEqual "http://localhost:8080/"
    }
}