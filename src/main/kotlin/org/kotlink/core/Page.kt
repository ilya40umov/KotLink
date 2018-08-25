package org.kotlink.core

import kotlin.math.max

data class Page<T>(
    val records: List<T>,
    val offset: Int,
    val limit: Int,
    val totalCount: Int
) {
    fun prevOffset(): Int = max(offset - limit, 0)

    fun nextOffset(): Int = (offset + limit).let { if (it < totalCount) it else offset }
}