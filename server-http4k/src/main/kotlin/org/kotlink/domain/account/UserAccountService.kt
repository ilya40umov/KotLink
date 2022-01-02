package org.kotlink.domain.account

import java.util.UUID

class UserAccountService(
    private val userAccountRepository: UserAccountRepository
) {

    fun findByUserEmail(userEmail: String): UserAccount? =
        userAccountRepository.findByUserEmail(userEmail)

    fun findOrCreateAccountForEmail(userEmail: String): UserAccount =
        findByUserEmail(userEmail) ?: createAccount(userEmail)

    private fun createAccount(email: String) = UserAccount(
        email = email,
        apiSecret = generateApiSecret()
    ).also { account ->
        userAccountRepository.createAccount(account)
    }

    companion object {
        private fun generateApiSecret() = UUID.randomUUID().toString().replace("-", "")
    }
}