package org.kotlink.core.cache

import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldContainAll
import org.junit.jupiter.api.Test
import org.kotlink.INBOX_ALIAS
import org.kotlink.INIT_ALIAS

class JacksonCacheValueSerializerTest {

    private val serializer = JacksonCacheValueSerializer()

    @Test
    fun deserialize_canHandleOutputOfSerialize_givenNullWasSerialized() {
        serializer.deserialize(serializer.serialize(null)) shouldBe null
    }

    @Test
    fun deserialize_canHandleOutputOfSerialize_givenAliasWasSerialized() {
        serializer.deserialize(serializer.serialize(INBOX_ALIAS)) shouldBeEqualTo INBOX_ALIAS
    }

    @Test
    fun deserialize_canHandleOutputOfSerialize_givenListOfAliasesWasSerialized() {
        serializer.deserialize(serializer.serialize(listOf(INBOX_ALIAS, INIT_ALIAS))).also { output ->
            output shouldBeInstanceOf List::class
            (output as List<*>) shouldContainAll listOf(INBOX_ALIAS, INIT_ALIAS)
        }
    }
}