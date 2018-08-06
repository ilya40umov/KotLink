package org.kotlink.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter

/**
 * Forces the server to immediately change the protocol to HTTPS (via a redirect).
 */
@Order(1)
@Configuration
@ConditionalOnProperty("kotlink.security.require-ssl")
class SslWebSecurityConfig : WebSecurityConfigurerAdapter() {

    override fun configure(http: HttpSecurity) {
        http
            .requiresChannel()
            .antMatchers("/actuator/**")
            .requiresInsecure()
            .and()
            .requiresChannel()
            .anyRequest()
            .requiresSecure()
            .and()
            .portMapper()
            .http(8080)
            .mapsTo(443)
    }
}