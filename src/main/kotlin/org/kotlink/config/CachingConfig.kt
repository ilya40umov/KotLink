package org.kotlink.config

import mu.KLogging
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Configuration
import javax.annotation.PostConstruct

@EnableCaching
@Configuration
class CachingConfig {

    @PostConstruct
    fun postConstruct() {
        logger.info { "Caching has been enabled." }
    }

    companion object : KLogging()
}