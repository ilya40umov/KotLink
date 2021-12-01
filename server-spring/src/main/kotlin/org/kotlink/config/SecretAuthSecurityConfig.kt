package org.kotlink.config

import mu.KLogging
import org.kotlink.api.security.SecretAuthFilter
import org.kotlink.api.security.SecretValidator
import org.kotlink.core.secret.ApiSecretService
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter
import org.springframework.security.web.util.matcher.AntPathRequestMatcher

@Order(SECRET_AUTH_SECURITY_CONFIG_ORDER)
@Configuration
@EnableWebSecurity
@Profile("!mvc-test")
class SecretAuthSecurityConfig(
    private val apiSecretService: ApiSecretService
) : WebSecurityConfigurerAdapter() {

    override fun configure(http: HttpSecurity) {
        http
            // XXX we limit the scope of secret-based authentication to a minimal set of endpoints
            // the rest of the API will be covered under OAuth
            .requestMatchers()
            .antMatchers("/api/link/suggest")
            .and()
            .addFilterBefore(
                SecretAuthFilter(
                    authPathRequestMatcher = AntPathRequestMatcher("/api/**"),
                    authenticationManager = authenticationManagerBean()
                ),
                AnonymousAuthenticationFilter::class.java
            )
            .authorizeRequests()
            .antMatchers("/api/**")
            .authenticated()
            .and()
            .anonymous()
            .disable()
            .csrf()
            .disable()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authenticationProvider(SecretValidator(apiSecretService))

        logger.info { "Secret-based security (for link suggestions) has been configured." }
    }

    companion object : KLogging()
}
