package org.kotlink.ui

import com.nhaarman.mockitokotlin2.whenever
import org.amshove.kluent.shouldEqual
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import javax.servlet.http.HttpServletRequest

@RunWith(MockitoJUnitRunner::class)
class ViewUtilsTest {

    @Mock
    private lateinit var request: HttpServletRequest

    private val viewUtils = ViewUtils()

    @Test
    fun `'serverUrlFromRequest' should only include port into URL if it's not standard`() {
        whenever(request.serverPort).thenReturn(8080)
        whenever(request.scheme).thenReturn("http")
        whenever(request.serverName).thenReturn("localhost")

        viewUtils.serverUrlFromRequest(request) shouldEqual "http://localhost:8080/"
    }
}