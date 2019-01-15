package org.kotlink.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.session.data.redis.config.ConfigureRedisAction

@Configuration
class RedisSessionConfig {
    @Bean
    fun configureRedisAction(): ConfigureRedisAction = ConfigureRedisAction.NO_OP
}