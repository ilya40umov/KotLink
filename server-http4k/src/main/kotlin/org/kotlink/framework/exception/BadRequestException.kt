package org.kotlink.framework.exception

class BadRequestException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)