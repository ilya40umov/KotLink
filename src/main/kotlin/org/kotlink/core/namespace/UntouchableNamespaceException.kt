package org.kotlink.core.namespace

import org.kotlink.core.KotLinkException

/** Indicates that the namespace can't be deleted / updated. */
class UntouchableNamespaceException(message: String) : KotLinkException(message)