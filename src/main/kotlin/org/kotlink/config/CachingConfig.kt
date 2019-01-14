package org.kotlink.config

import com.fasterxml.jackson.databind.ObjectMapper
import mu.KLogging
import org.flywaydb.core.Flyway
import org.kotlink.core.cache.JacksonCacheValueSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.serializer.RedisSerializationContext
import java.time.Duration
import javax.annotation.PostConstruct


@EnableCaching
@Configuration
class CachingConfig {

    @PostConstruct
    fun postConstruct() {
        logger.info { "Caching has been enabled." }
    }

    @Bean
    @ConditionalOnProperty("spring.cache.type", havingValue = "redis")
    fun redisCacheConfiguration(
        objectMapper: ObjectMapper,
        flyway: Flyway,
        flywayMigrationInitializer: FlywayMigrationInitializer,
        @Value("\${spring.cache.redis.time-to-live}") timeToLive: Duration
    ): RedisCacheConfiguration {
        val currentMigrationVersion = flyway.info().current().version.version
        logger.info { "Determined current migration version as: $currentMigrationVersion." }
        return RedisCacheConfiguration.defaultCacheConfig()
            .computePrefixWith { cacheName ->
                "kotlink:$currentMigrationVersion:$cacheName:"
            }
            .entryTtl(timeToLive)
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair
                    .fromSerializer(JacksonCacheValueSerializer())
            )
    }

    @Bean
    @ConditionalOnProperty("spring.cache.type", havingValue = "redis")
    fun redisCacheManager(
        redisConnectionFactory: RedisConnectionFactory,
        redisCacheConfiguration: RedisCacheConfiguration
    ): RedisCacheManager {
        return RedisCacheManager
            .builder(redisConnectionFactory)
            .cacheDefaults(redisCacheConfiguration)
            .build()
            .also { logger.info { "Created Redis-based cache manager." } }
    }

    companion object : KLogging()
}