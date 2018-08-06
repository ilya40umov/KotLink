package org.kotlink.config

import mu.KLogging
import org.kotlink.core.account.UserAccountService
import org.kotlink.core.oauth.OAuthAuthoritiesExtractor
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso
import org.springframework.boot.autoconfigure.security.oauth2.resource.AuthoritiesExtractor
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.security.web.util.matcher.RequestMatcher
import javax.servlet.http.HttpSession

@Order(2)
@Profile("!repotest")
@Configuration
@ConfigurationProperties("kotlink.security.oauth")
@EnableWebSecurity
@EnableOAuth2Sso
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
class OAuthSecurityConfig : WebSecurityConfigurerAdapter() {

    val allowedEmails = mutableSetOf<String>()

    lateinit var allowedEmailRegex: String

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http
            .requiresChannel()
            .requestMatchers(RequestMatcher { it.getHeader("X-Forwarded-Proto") == "http" })
            // forcing a redirect to https if we are behind a proxy / LB
            .requiresSecure()
            .and()
            .authorizeRequests()
            .antMatchers(
                "/login**",
                "/callback/",
                "/webjars/**",
                "/error**",
                "/js/**",
                "/css/**",
                "/actuator/health",
                "/favicon.ico"
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

        logger.info { "OAuth security has been configured." }
    }

    @Bean
    fun authoritiesExtractor(
        session: HttpSession,
        userAccountService: UserAccountService
    ): AuthoritiesExtractor = OAuthAuthoritiesExtractor(
        session = session,
        allowedEmails = allowedEmails,
        allowedEmailRegex = allowedEmailRegex.toRegex(),
        userAccountService = userAccountService
    )

    companion object : KLogging()
}
