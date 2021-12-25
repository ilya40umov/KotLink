package org.kotlink.framework.ui

import org.http4k.template.ViewModel

class UiViewModel(
    private val template: String,
    val data: Map<String, *>
) : ViewModel {
    override fun template(): String = template
}