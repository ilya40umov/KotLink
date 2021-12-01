package org.kotlink.ui

import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView
import javax.servlet.RequestDispatcher
import javax.servlet.http.HttpServletRequest

/**
 * Called by [org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController],
 * whenever the client expects "text/html".
 */
@Component
class UiErrorViewResolver : ErrorViewResolver {

    override fun resolveErrorView(
        request: HttpServletRequest,
        status: HttpStatus,
        model: MutableMap<String, Any>
    ): ModelAndView? {
        val requestUri = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI) as String?
        if (requestUri == null || !requestUri.startsWith("/api")) {
            val errorPage = when {
                status == HttpStatus.UNAUTHORIZED -> "error/401"
                status == HttpStatus.NOT_FOUND -> "error/404"
                status.is4xxClientError -> "error/4xx"
                else -> "error/500"
            }
            return ModelAndView(errorPage)
        }
        return null
    }
}