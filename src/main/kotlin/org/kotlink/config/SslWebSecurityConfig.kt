package org.kotlink.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.web.util.matcher.RequestMatcher

/**
 * Makes sure the server is going to use HTTPS in URLs when replying to a request
 * that is coming from behind a loadbalancer.
 */
@Order(1)
@Configuration
@ConditionalOnProperty("kotlink.security.require-ssl")
class SslWebSecurityConfig : WebSecurityConfigurerAdapter() {

    override fun configure(http: HttpSecurity) {
        http
            .requiresChannel()
            .requestMatchers(RequestMatcher { it.getHeader("X-Forwarded-Proto") != null })
            .requiresSecure()
    }
}