package org.kotlink.config

import ch.qos.logback.access.tomcat.LogbackValve
import mu.KLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory
import org.springframework.boot.web.server.WebServerFactoryCustomizer
import org.springframework.context.annotation.Configuration

@Configuration
class TomcatWebServerCustomizer(
    @Value("\${kotlink.logging.enable-access-log}")
    private val enableAccessLog: Boolean
) : WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

    override fun customize(factory: TomcatServletWebServerFactory) {
        if (enableAccessLog) {
            logger.info { "Enabling redirection of Tomcat access logs to Logback." }
            factory.addContextValves(
                LogbackValve().apply { filename = "logback-access.xml" }
            )
        }
    }

    companion object : KLogging()
}