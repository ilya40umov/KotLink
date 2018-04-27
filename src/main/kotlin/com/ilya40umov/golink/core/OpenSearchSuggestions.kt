package com.ilya40umov.golink.core

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.ilya40umov.golink.alias.Alias

/**
 * Suggestions in a format defined by:
 * http://www.opensearch.org/Specifications/OpenSearch/Extensions/Suggestions/1.1
 */
@JsonSerialize(using = OpenSearchSuggestionsSerializer::class)
data class OpenSearchSuggestions(
    val prefix: String,
    val links: List<String>,
    val descriptions: List<String>,
    val redirectUrls: List<String>
) {
    constructor(prefix: String, aliases: List<Alias>) : this(
        prefix = prefix,
        links = aliases.map { it.fullLink },
        descriptions = aliases.map { it.fullLink },
        redirectUrls = aliases.map { it.redirectUrl }
    )
}

class OpenSearchSuggestionsSerializer(t: Class<OpenSearchSuggestions>? = null)
    : StdSerializer<OpenSearchSuggestions>(t) {

    override fun serialize(value: OpenSearchSuggestions?, gen: JsonGenerator?, provider: SerializerProvider?) {
        gen?.run {
            value?.also { suggestions ->
                writeStartArray()
                writeString(suggestions.prefix)
                writeStringArray(suggestions.links)
                writeStringArray(suggestions.descriptions)
                writeStringArray(suggestions.redirectUrls)
                writeEndArray()
            }
        }
    }

    private fun JsonGenerator.writeStringArray(values: List<String>) {
        writeStartArray()
        values.forEach(this::writeString)
        writeEndArray()
    }
}