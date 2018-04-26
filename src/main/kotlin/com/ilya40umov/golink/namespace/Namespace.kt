package com.ilya40umov.golink.namespace

/**
 * Groups aliases that start with a common keyword, which is very helpful for isolating aliases between teams/projects.
 */
data class Namespace(
    val id: Int,
    val keyword: String
)