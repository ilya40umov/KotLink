package org.kotlink.ui.alias

import org.kotlink.core.alias.Alias
import org.kotlink.framework.mvc.BaseViewModel

data class ListAliases(
    val input: String,
    val aliases: List<Alias>
) : BaseViewModel