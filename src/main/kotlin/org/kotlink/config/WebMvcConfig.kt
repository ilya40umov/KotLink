package org.kotlink.config

import org.kotlink.ui.SelectViewInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebMvcConfig : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(SelectViewInterceptor())
    }

    override fun addViewControllers(registry: ViewControllerRegistry) {
        registry
            .addRedirectViewController("/", "/ui/search")
            .setKeepQueryParams(false)
            .setStatusCode(HttpStatus.TEMPORARY_REDIRECT)

        registry
            .addRedirectViewController("/ui/sign_out_success", "/ui/search")
            .setKeepQueryParams(false)
            .setStatusCode(HttpStatus.TEMPORARY_REDIRECT)
    }
}