package org.kotlink.ui

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.kotlink.TEST_ACCOUNT
import org.kotlink.core.CurrentUser
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.web.context.annotation.RequestScope

@Configuration
@Profile("!integration-test")
class UiTestConfig {

    @Bean
    @RequestScope
    fun viewUtils() = ViewUtils()

    @Bean
    @RequestScope
    fun currentUser(): CurrentUser = mock {
        on { getEmail() } doReturn TEST_ACCOUNT.email
    }
}