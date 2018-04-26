package com.ilya40umov.golink

import com.ilya40umov.golink.alias.Alias
import com.ilya40umov.golink.namespace.Namespace

val DEFAULT_NAMESPACE = Namespace(id = 1, keyword = "")

val INBOX_ALIAS = Alias(
    id = 1,
    namespace = DEFAULT_NAMESPACE,
    link = "inbox",
    redirectUrl = "https://inbox.google.com/"
)

val INIT_ALIAS = Alias(
    id = 2,
    namespace = DEFAULT_NAMESPACE,
    link = "init",
    redirectUrl = "https://en.wikipedia.org/wiki/Systemd"
)