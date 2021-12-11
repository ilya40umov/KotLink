package org.kotlink.ui.alias

import org.kotlink.core.alias.Alias

data class ListAliasesModel(
    val input: String,
    val aliases: List<Alias>
)