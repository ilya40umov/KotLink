package org.kotlink.core.exposed

class DatabaseException(message: String? = null, cause: Throwable? = null) : RuntimeException(message, cause) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}