package org.kotlink.ui

import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

enum class UiView(val title: String) {
    SEARCH("KotLink - Search"),
    LIST_ALIASES("KotLink - Aliases"),
    NEW_ALIAS("KotLink - New Alias"),
    EDIT_ALIAS("KotLink - Edit Alias"),
    LIST_NAMESPACES("KotLink - Namespaces"),
    NEW_NAMESPACE("KotLink - New Namespace"),
    EDIT_NAMESPACE("KotLink - Edit Namespace"),
    EXTENSION_SECRET("KotLink - Extension Secret")
}

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.RUNTIME)
annotation class SelectView(val value: UiView)

class SelectViewInterceptor : HandlerInterceptorAdapter() {

    override fun postHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        modelAndView: ModelAndView?
    ) {
        when (handler) {
            !is HandlerMethod -> return
            else -> {
                val selectView = handler.getMethodAnnotation(SelectView::class.java)
                if (selectView != null && modelAndView != null) {
                    modelAndView.addObject("selectedView", selectView.value)
                }
            }
        }
    }
}
