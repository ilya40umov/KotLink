package org.kotlink.shared.config

import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso
import org.springframework.boot.autoconfigure.security.oauth2.resource.AuthoritiesExtractor
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.Order
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import javax.servlet.http.HttpSession

@Order(2)
@Profile("!repotest")
@Configuration
@ConfigurationProperties("kotlink.security.ui")
@EnableWebSecurity
@EnableOAuth2Sso
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
class OAuthSecurityConfig : WebSecurityConfigurerAdapter() {

    val allowedAddresses = mutableListOf<String>()

    val allowedOrgSuffixes = mutableListOf<String>()

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http
            .authorizeRequests()
            .antMatchers(
                "/login**",
                "/callback/",
                "/webjars/**",
                "/error**",
                "/js/**",
                "/css/**"
            )
            .permitAll()
            .and()
            .logout()
            .logoutRequestMatcher(AntPathRequestMatcher("/ui/sign_out"))
            .logoutSuccessUrl("/ui/sign_out_success")
            .deleteCookies("JSESSIONID")
            .invalidateHttpSession(true)
            .permitAll()
            .and()
            .authorizeRequests()
            .anyRequest()
            .authenticated()
    }

    @Bean
    fun authoritiesExtractor(session: HttpSession): AuthoritiesExtractor {
        return AuthoritiesExtractor {
            val email = it["email"].toString()
            if (!(allowedAddresses.contains(email) || allowedOrgSuffixes.any { email.endsWith(it) })) {
                session.invalidate()
                throw BadCredentialsException("You don't have access to this KotLink server!")
            }
            AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_USER")
        }
    }
}
