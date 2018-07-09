package org.kotlink

import org.kotlink.core.alias.Alias
import org.kotlink.core.namespace.Namespace
import org.kotlink.core.secret.ApiSecret

val DEFAULT_NAMESPACE = Namespace(id = 1, keyword = "")

const val ABC_NAMESPACE_ID = 2L

val ABC_NAMESPACE = Namespace(id = ABC_NAMESPACE_ID, keyword = "abc")

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

val TEST_SECRET = ApiSecret(
    secret = "extension_secret_123",
    userEmail = "test123@gmail.com"
)