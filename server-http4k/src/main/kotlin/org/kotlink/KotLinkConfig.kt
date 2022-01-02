package org.kotlink

import org.http4k.core.Uri
import org.kotlink.KotLinkConfig.Companion.LOCAL_PORT

fun loadConfig(environment: Environment): KotLinkConfig {
    var config = KotLinkConfig()
    if (environment == Environment.AWS) {
        config = config.copy(
            googleOAuth = config.googleOAuth.copy(
                callbackUri = System.getenv("GOOGLE_OAUTH_CALLBACK_URI")?.let(Uri::of)
                    ?: config.googleOAuth.callbackUri
            )
        )
    } else if (environment == Environment.LOCAL) {
        config = config.copy(
            hotReload = true,
            dynamoDb = config.dynamoDb.copy(
                endpointOverride = Uri.of("http://localhost:4566")
            )
        )
    }
    return config
}

data class KotLinkConfig(
    val hotReload: Boolean = false,
    val googleOAuth: GoogleOAuthConfig = GoogleOAuthConfig(),
    val cookieEncryption: CookieEncryptionConfig = CookieEncryptionConfig(),
    val dynamoDb: DynamoDbConfig = DynamoDbConfig()
) {
    companion object {
        const val LOCAL_PORT = 9090
    }
}

data class GoogleOAuthConfig(
    val googleClientId: String = "115327279391-cqrf3suvt416skdkr8lqvdntgfa90epg.apps.googleusercontent.com",
    val googleClientSecret: String = "SZDICodbaLAkNXjbFKfOFZCO",
    val callbackUri: Uri = Uri.of("http://localhost:$LOCAL_PORT/login/oauth2/code/google")
)

data class CookieEncryptionConfig(
    // openssl enc -aes-128-cbc -k secret -P -md sha1
    val encryptionKey: String = "98DF0848919E4FB12A0C0F3DA24C954A"
)

data class DynamoDbConfig(
    val tableName: String = "kotlink",
    val endpointOverride: Uri? = null
)