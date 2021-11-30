package org.kotlink.core.alias

import org.kotlink.core.KotLinkException

/** Indicates that the full link represented by alias is already taken. */
class FullLinkExistsException(message: String) : KotLinkException(message)