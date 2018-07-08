package org.kotlink.config

import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@EnableCaching
@Profile("!repotest")
@Configuration
class CachingConfig