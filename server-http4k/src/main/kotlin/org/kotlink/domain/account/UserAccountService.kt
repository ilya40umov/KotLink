package org.kotlink.domain.account

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class UserAccountService {

    private val userAccounts = ConcurrentHashMap<String, UserAccount>()

    init {
        val zorroAccount = UserAccount(email = "zorro@gmail.com", apiSecret = "abcdef")
        userAccounts += zorroAccount.email to zorroAccount
    }

    fun findByUserEmail(userEmail: String): UserAccount? = userAccounts[userEmail]

    fun findOrCreateAccountForEmail(userEmail: String): UserAccount =
        findByUserEmail(userEmail) ?: createAccount(userEmail)

    private fun createAccount(email: String) = UserAccount(
        email = email,
        apiSecret = generateApiSecret()
    ).also { account ->
        userAccounts[account.email] = account
    }

    companion object {
        private fun generateApiSecret() = UUID.randomUUID().toString().replace("-", "")
    }
}