package org.kotlink.ui.help

import org.kotlink.framework.mvc.BaseViewModel

data class SetupInstructions(
    val apiSecret: String
) : BaseViewModel