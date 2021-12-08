package org.kotlink.ui.namespace

import org.kotlink.core.namespace.Namespace
import org.kotlink.framework.mvc.BaseViewModel

data class ListNamespaces(
    val namespaces: List<Namespace>
) : BaseViewModel