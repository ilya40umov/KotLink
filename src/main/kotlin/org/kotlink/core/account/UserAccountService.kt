package org.kotlink.core.account

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class UserAccountService(private val userAccountRepo: UserAccountRepo) {

    fun findByUserEmail(userEmail: String): UserAccount? =
        userAccountRepo.findByUserEmail(userEmail)

    fun findOrCreateAccountForEmail(userEmail: String): UserAccount =
        userAccountRepo.findByUserEmail(userEmail) ?: userAccountRepo.insert(
            UserAccount(
                email = userEmail
            )
        )
}