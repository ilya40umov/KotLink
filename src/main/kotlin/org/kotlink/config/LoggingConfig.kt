package org.kotlink.config

import ch.qos.logback.access.tomcat.LogbackValve
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class LoggingConfig {

    @Bean
    @ConditionalOnProperty(name = ["kotlink.logging.enable-access-log"])
    fun servletContainerFactory(): TomcatServletWebServerFactory {
        val logbackValve = LogbackValve().apply { filename = "logback-access.xml" }
        return TomcatServletWebServerFactory().apply {
            addContextValves(logbackValve)
        }
    }
}