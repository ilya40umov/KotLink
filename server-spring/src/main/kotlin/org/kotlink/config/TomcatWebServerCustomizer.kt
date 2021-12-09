package org.kotlink.config

import mu.KLogging
import org.apache.catalina.valves.AbstractAccessLogValve
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory
import org.springframework.boot.web.server.WebServerFactoryCustomizer
import org.springframework.context.annotation.Configuration
import java.io.CharArrayWriter

@Configuration
class TomcatWebServerCustomizer(
    @Value("\${kotlink.logging.enable-access-log}")
    private val enableAccessLog: Boolean
) : WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

    override fun customize(factory: TomcatServletWebServerFactory) {
        if (enableAccessLog) {
            logger.info { "Enabling redirection of Tomcat access logs to Log4j2." }
            factory.addContextValves(
                object : AbstractAccessLogValve() {
                    val accessLogger = LoggerFactory.getLogger("accesslog")
                    override fun log(message: CharArrayWriter?) {
                        accessLogger.info(message.toString())
                    }
                }.apply {
                    enabled = true
                    pattern = """%h "%r" %s %b "%{Referer}i" "%{User-Agent}i""""
                }
            )
        }
    }

    companion object : KLogging()
}