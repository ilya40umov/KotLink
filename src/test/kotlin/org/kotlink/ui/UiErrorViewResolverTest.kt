package org.kotlink.ui

import com.nhaarman.mockitokotlin2.whenever
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldEqual
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpStatus
import javax.servlet.RequestDispatcher
import javax.servlet.http.HttpServletRequest

@ExtendWith(MockitoExtension::class)
class UiErrorViewResolverTest(
    @Mock private val request: HttpServletRequest
) {

    private val resolver = UiErrorViewResolver()

    @Test
    fun `'resolveErrorView' should return view if called for API endpoint`() {
        whenever(request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI))
            .thenReturn("/api")

        resolver.resolveErrorView(request, HttpStatus.NOT_FOUND, mutableMapOf()).also {
            it shouldBe null
        }
    }

    @Test
    fun `'resolveErrorView' should return 401 page if status is UNAUTHORIZED`() {
        resolver.resolveErrorView(request, HttpStatus.UNAUTHORIZED, mutableMapOf()).also {
            it?.viewName shouldEqual "error/401"
        }
    }

    @Test
    fun `'resolveErrorView' should return 404 page if status is NOT_FOUND`() {
        resolver.resolveErrorView(request, HttpStatus.NOT_FOUND, mutableMapOf()).also {
            it?.viewName shouldEqual "error/404"
        }
    }

    @Test
    fun `'resolveErrorView' should return 4xx page if status is a client error but not 401 or 404`() {
        resolver.resolveErrorView(request, HttpStatus.BAD_REQUEST, mutableMapOf()).also {
            it?.viewName shouldEqual "error/4xx"
        }
    }

    @Test
    fun `'resolveErrorView' should return 500 page if status is INTERNAL_SERVER_ERROR`() {
        resolver.resolveErrorView(request, HttpStatus.INTERNAL_SERVER_ERROR, mutableMapOf()).also {
            it?.viewName shouldEqual "error/500"
        }
    }

    @Test
    fun `'resolveErrorView' should return 500 page if status is not specifically handled`() {
        resolver.resolveErrorView(request, HttpStatus.BAD_GATEWAY, mutableMapOf()).also {
            it?.viewName shouldEqual "error/500"
        }
    }
}