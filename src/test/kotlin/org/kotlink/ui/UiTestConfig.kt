package org.kotlink.ui

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.context.annotation.RequestScope

@Configuration
class UiTestConfig {

    @Bean
    @RequestScope
    fun viewUtils() = ViewUtils()
}