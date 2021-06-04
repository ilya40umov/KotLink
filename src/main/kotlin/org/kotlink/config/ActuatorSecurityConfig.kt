package org.kotlink.config

import mu.KLogging
import org.kotlink.core.security.ActuatorAuthenticationProvider
import org.springframework.boot.autoconfigure.security.SecurityProperties
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter

@Order(ACTUATOR_SECURITY_CONFIG_ORDER)
@Configuration
@EnableWebSecurity
@Profile("!mvc-test")
class ActuatorSecurityConfig(
    private val securityProperties: SecurityProperties
) : WebSecurityConfigurerAdapter() {

    override fun configure(http: HttpSecurity) {
        http
            .requestMatchers()
            .antMatchers("/actuator/*")
            .and()
            .authorizeRequests()
            .mvcMatchers("/actuator/health")
            .permitAll()
            .mvcMatchers("/actuator/**")
            .hasRole("ACTUATOR")
            .anyRequest()
            .authenticated()
            .and()
            .httpBasic()
            .and()
            .authenticationProvider(ActuatorAuthenticationProvider(securityProperties))

        logger.info { "HTTP Basic security (for actuator endpoints) has been configured." }
    }

    companion object : KLogging()
}
