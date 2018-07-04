package org.kotlink.core.secret

import org.springframework.stereotype.Service

@Service
class ApiSecretService {

    fun findBySecret(secret: String): ApiSecret? {
        return if (secret == "42") {
            ApiSecret(secret, "unknown@gmail.com")
        } else {
            null
        }
    }

}