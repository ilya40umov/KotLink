package org.kotlink.core.secret

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class ApiSecretService(private val apiSecretRepo: ApiSecretRepo) {

    fun findBySecret(secret: String) =
        apiSecretRepo.findBySecret(secret)

    fun findOrCreateForEmail(userEmail: String) =
        apiSecretRepo.findByUserEmail(userEmail).let {
            when (it) {
                null -> apiSecretRepo.insert(
                    ApiSecret(
                        secret = UUID.randomUUID().toString().replace("-", ""),
                        userEmail = userEmail
                    )
                )
                else -> it
            }
        }
}