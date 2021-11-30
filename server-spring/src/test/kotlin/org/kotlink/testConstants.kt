package org.kotlink

import org.kotlink.core.account.UserAccount
import org.kotlink.core.alias.Alias
import org.kotlink.core.namespace.Namespace
import org.kotlink.core.secret.ApiSecret

val TEST_ACCOUNT = UserAccount(
    id = 1,
    email = "zorro@gmail.com"
)

val DEFAULT_NAMESPACE = Namespace(id = 1, keyword = "", ownerAccount = TEST_ACCOUNT)

const val ABC_NAMESPACE_ID = 2L

val ABC_NAMESPACE = Namespace(id = ABC_NAMESPACE_ID, keyword = "abc", ownerAccount = TEST_ACCOUNT)

val INBOX_ALIAS = Alias(
    id = 1,
    namespace = DEFAULT_NAMESPACE,
    link = "inbox",
    redirectUrl = "https://inbox.google.com/",
    ownerAccount = TEST_ACCOUNT,
    description = "Inbox Alias"
)

val INIT_ALIAS = Alias(
    id = 2,
    namespace = DEFAULT_NAMESPACE,
    link = "init",
    redirectUrl = "https://en.wikipedia.org/wiki/Systemd",
    ownerAccount = TEST_ACCOUNT
)

val TEST_SECRET = ApiSecret(
    secret = "extension_secret_123",
    userAccount = TEST_ACCOUNT
)
