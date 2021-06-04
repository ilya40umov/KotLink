package org.kotlink.config

import mu.KLogging
import org.kotlink.core.account.UserAccountService
import org.kotlink.core.oauth.OAuthAuthoritiesMapper
import org.kotlink.core.oauth.OAuthFailureHandler
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.security.web.util.matcher.RequestMatcher
import javax.servlet.http.HttpSession

@Order(OAUTH_SECURITY_CONFIG_ORDER)
@Configuration
@ConfigurationProperties("kotlink.security.oauth")
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
@Profile("!mvc-test")
class OAuthSecurityConfig : WebSecurityConfigurerAdapter() {

    val allowedEmails = mutableSetOf<String>()

    lateinit var allowedEmailRegex: String

    @Throws(Exception::class)
    @Suppress("ELValidationInJSP", "SpringElInspection")
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
                "/oauth/**",
                "/error**",
                "/js/**",
                "/css/**",
                "/img/**",
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
            // prevents ACTUATOR user from being used to access UI
            .access("not(hasRole('ROLE_ACTUATOR')) and authenticated")
            .and()
            .oauth2Login()
            .loginPage("/login")
            .failureHandler(OAuthFailureHandler())

        logger.info { "OAuth security has been configured." }
    }

    @Bean
    fun authoritiesMapper(
        session: HttpSession,
        userAccountService: UserAccountService
    ): GrantedAuthoritiesMapper = OAuthAuthoritiesMapper(
        session = session,
        allowedEmails = allowedEmails,
        allowedEmailRegex = allowedEmailRegex.toRegex(),
        userAccountService = userAccountService
    )

    companion object : KLogging()
}
