package org.kotlink.core.secret

import org.kotlink.core.account.UserAccount

data class ApiSecret(
    val id: Long = 0,
    val secret: String,
    val userAccount: UserAccount
)