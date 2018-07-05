package org.kotlink.ui

import com.nhaarman.mockitokotlin2.mock
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.context.annotation.RequestScope

@Configuration
class UiTestConfig {

    @Bean
    @RequestScope
    fun viewUtils() = ViewUtils()

    @Bean
    @RequestScope
    fun currentUser() = mock<CurrentUser>()
}