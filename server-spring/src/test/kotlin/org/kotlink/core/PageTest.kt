package org.kotlink.core

import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.kotlink.core.alias.Alias

class PageTest {

    @Test
    fun `'prevOffset' should return current offset minus page size if it is a positive number`() {
        Page(
            records = emptyList<Alias>(),
            offset = 15,
            limit = 10,
            totalCount = 0
        ).prevOffset() shouldBeEqualTo 5
    }

    @Test
    fun `'prevOffset' should return 0 if current offset is less than the page size`() {
        Page(
            records = emptyList<Alias>(),
            offset = 3,
            limit = 10,
            totalCount = 0
        ).prevOffset() shouldBeEqualTo 0
    }

    @Test
    fun `'nextOffset' should return current offset plus page size if it is less than the total count`() {
        Page(
            records = emptyList<Alias>(),
            offset = 0,
            limit = 10,
            totalCount = 20
        ).nextOffset() shouldBeEqualTo 10
    }

    @Test
    fun `'nextOffset' should return current offset if adding page size will place it outsize of the total count`() {
        Page(
            records = emptyList<Alias>(),
            offset = 0,
            limit = 10,
            totalCount = 8
        ).nextOffset() shouldBeEqualTo 0
    }
}