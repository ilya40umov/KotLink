package org.kotlink.config

import org.kotlink.api.security.SecretAuthFilter
import org.kotlink.api.security.SecretValidator
import org.kotlink.core.secret.ApiSecretService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter
import org.springframework.security.web.util.matcher.AntPathRequestMatcher

@Order(2)
@Profile("!repotest")
@Configuration
@EnableWebSecurity
class SecretAuthSecurityConfig : WebSecurityConfigurerAdapter() {

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
    }

    @Bean
    fun secretValidator(apiSecretService: ApiSecretService) = SecretValidator(apiSecretService)
}
