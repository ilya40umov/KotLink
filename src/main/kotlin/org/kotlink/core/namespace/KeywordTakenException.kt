package org.kotlink.core.namespace

import org.kotlink.core.KotLinkException

/** Indicates that the request keyword is taken by another namespace. */
class KeywordTakenException(message: String) : KotLinkException(message)