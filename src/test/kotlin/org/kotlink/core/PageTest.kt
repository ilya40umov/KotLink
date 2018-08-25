package org.kotlink.core

import org.amshove.kluent.shouldEqual
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.kotlink.core.alias.Alias

@RunWith(JUnit4::class)
class PageTest {

    @Test
    fun `'prevOffset' should return current offset minus page size if it is a positive number`() {
        Page(
            records = emptyList<Alias>(),
            offset = 15,
            limit = 10,
            totalCount = 0
        ).prevOffset() shouldEqual 5
    }

    @Test
    fun `'prevOffset' should return 0 if current offset is less than the page size`() {
        Page(
            records = emptyList<Alias>(),
            offset = 3,
            limit = 10,
            totalCount = 0
        ).prevOffset() shouldEqual 0
    }

    @Test
    fun `'nextOffset' should return current offset plus page size if it is less than the total count`() {
        Page(
            records = emptyList<Alias>(),
            offset = 0,
            limit = 10,
            totalCount = 20
        ).nextOffset() shouldEqual 10
    }

    @Test
    fun `'nextOffset' should return current offset if adding page size will place it outsize of the total count`() {
        Page(
            records = emptyList<Alias>(),
            offset = 0,
            limit = 10,
            totalCount = 8
        ).nextOffset() shouldEqual 0
    }
}