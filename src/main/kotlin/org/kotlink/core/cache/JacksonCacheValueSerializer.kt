package org.kotlink.core.cache

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.data.redis.serializer.RedisSerializer

class JacksonCacheValueSerializer : RedisSerializer<Any> {

    private val objectMapper =
        jacksonObjectMapper()
            .enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY)

    override fun serialize(t: Any?): ByteArray? {
        return objectMapper.writeValueAsBytes(CacheValue(value = t))
    }

    override fun deserialize(bytes: ByteArray?): Any? {
        return bytes?.let { objectMapper.readValue<CacheValue>(it).value }
    }

    companion object {
        data class CacheValue(val value: Any?)
    }
}