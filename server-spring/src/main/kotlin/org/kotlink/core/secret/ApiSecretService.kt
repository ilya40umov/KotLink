package org.kotlink.core.secret

import org.kotlink.core.account.UserAccountService
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class ApiSecretService(
    private val apiSecretRepo: ApiSecretRepo,
    private val userAccountService: UserAccountService
) {

    @Cacheable(cacheNames = [API_SECRET_CACHE], unless = "#result == null")
    fun findBySecret(secret: String): ApiSecret? =
        apiSecretRepo.findBySecret(secret)

    fun findOrCreateForEmail(userEmail: String): ApiSecret {
        val userAccount = userAccountService.findOrCreateAccountForEmail(userEmail)
        return apiSecretRepo.findByUserEmail(userEmail) ?: apiSecretRepo.insert(
            ApiSecret(
                secret = UUID.randomUUID().toString().replace("-", ""),
                userAccount = userAccount
            )
        )
    }

    companion object {
        const val API_SECRET_CACHE = "apiSecret"
    }
}