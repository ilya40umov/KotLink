package org.kotlink.ui

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.kotlink.TEST_SECRET
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
    fun currentUser(): CurrentUser = mock {
        on { getEmail() } doReturn TEST_SECRET.userEmail
    }
}