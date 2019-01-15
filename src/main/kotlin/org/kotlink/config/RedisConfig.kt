package org.kotlink.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.session.data.redis.config.ConfigureRedisAction

@Configuration
class RedisConfig {
    companion object {
        // workaround for https://github.com/spring-projects/spring-session/issues/124
        @Bean
        @JvmStatic
        fun configureRedisAction(): ConfigureRedisAction {
            return ConfigureRedisAction.NO_OP
        }
    }
}