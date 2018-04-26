package com.ilya40umov.golink.core

import com.fasterxml.jackson.databind.ObjectMapper
import com.gregwoodfill.assert.shouldEqualJson
import com.ilya40umov.golink.INBOX_ALIAS
import com.ilya40umov.golink.INIT_ALIAS
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class OpenSearchSuggestionsSerializerTest {

    private val objectMapper = ObjectMapper()

    @Test
    fun `serialize should produce JSON in format defined by OpenSearch spec`() {
        val suggestions = OpenSearchSuggestions(
            prefix = "in",
            links = listOf(INIT_ALIAS.link, INBOX_ALIAS.link),
            descriptions = listOf(INIT_ALIAS.link, INBOX_ALIAS.link),
            redirectUrls = listOf(INIT_ALIAS.redirectUrl, INBOX_ALIAS.redirectUrl)
        )

        val json = objectMapper.writeValueAsString(suggestions)

        json shouldEqualJson "['in', " +
            "['${INIT_ALIAS.link}', '${INBOX_ALIAS.link}'], " +
            "['${INIT_ALIAS.link}', '${INBOX_ALIAS.link}'], " +
            "['${INIT_ALIAS.redirectUrl}', '${INBOX_ALIAS.redirectUrl}']]"
    }

    @Test
    fun `serialize should return empty JSON lists when suggestion contains no links`() {
        val suggestions = OpenSearchSuggestions(
            prefix = "abc",
            links = emptyList(),
            descriptions = emptyList(),
            redirectUrls = emptyList()
        )

        val json = objectMapper.writeValueAsString(suggestions)

        json shouldEqualJson "['abc', [], [], []]"
    }
}