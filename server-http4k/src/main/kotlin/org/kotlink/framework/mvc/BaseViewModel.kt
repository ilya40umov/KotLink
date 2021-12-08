package org.kotlink.framework.mvc

import org.http4k.template.ViewModel

interface BaseViewModel : ViewModel {
    val klass: Class<*>
        get() = javaClass
}