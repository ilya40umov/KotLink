package org.kotlink.core

open class KotLinkException(override val message: String, cause: Throwable? = null) : RuntimeException(message, cause)