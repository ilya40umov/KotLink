package org.kotlink

import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.DEFAULT_ISOLATION_LEVEL
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.kotlink.core.account.UserAccount
import org.kotlink.core.account.UserAccountRepo
import org.kotlink.core.account.UserAccounts
import org.kotlink.core.alias.Alias
import org.kotlink.core.alias.AliasRepo
import org.kotlink.core.alias.Aliases
import org.kotlink.core.namespace.NamespaceRepo
import org.kotlink.core.secret.ApiSecret
import org.kotlink.core.secret.ApiSecretRepo
import org.kotlink.core.secret.ApiSecrets
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.util.Base64Utils
import java.nio.charset.StandardCharsets.UTF_8

@ExtendWith(SpringExtension::class)
@ActiveProfiles("local", "integration-test")
@SpringBootTest(webEnvironment = RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KotLinkSecurityAndIntegrationTest(
    @Autowired private val webClient: WebTestClient,
    @LocalServerPort private val serverPort: Int,
    @Autowired private val userAccountRepo: UserAccountRepo,
    @Autowired private val apiSecretRepo: ApiSecretRepo,
    @Autowired private val namespaceRepo: NamespaceRepo,
    @Autowired private val aliasRepo: AliasRepo,
    @Autowired private val cacheManager: RedisCacheManager
) {

    private val userAccount: UserAccount
    private val apiSecret: ApiSecret
    private val abcAlias: Alias

    init {
        val transaction = TransactionManager.currentOrNew(DEFAULT_ISOLATION_LEVEL)
        userAccount = userAccountRepo.insert(UserAccount(email = "security-test@kotlink.org"))
        apiSecret = apiSecretRepo.insert(ApiSecret(secret = "secret", userAccount = userAccount))
        abcAlias = aliasRepo.insert(
            Alias(
                id = 0,
                namespace = namespaceRepo.findByKeyword(keyword = "")!!,
                link = "abc",
                redirectUrl = "http://example.org",
                description = "Example dot org",
                ownerAccount = userAccount
            )
        )
        transaction.commit()
    }

    @AfterAll
    internal fun cleanUp() {
        cacheManager.cacheNames.forEach { cacheName ->
            cacheManager.getCache(cacheName)?.clear()
        }
        transaction {
            Aliases.deleteWhere { Aliases.id.eq(abcAlias.id) }
            ApiSecrets.deleteWhere { ApiSecrets.id.eq(apiSecret.id) }
            UserAccounts.deleteWhere { UserAccounts.id.eq(userAccount.id) }
            commit()
        }
    }

    @Test
    fun `app root should redirect to login page using HTTP given there is no proxy or LB in front`() {
        webClient.get().uri("http://localhost:$serverPort/")
            .exchange()
            .expectStatus().isFound
            .expectHeader().valueEquals("Location", "http://localhost:$serverPort/login")
    }

    @Test
    fun `app root should redirect to app root using HTTPS given there is LB in front and request is using HTTP`() {
        webClient.get().uri("http://localhost:$serverPort/")
            .header("X-Forwarded-Proto", "http")
            .exchange()
            .expectStatus().isFound
            .expectHeader().valueEquals("Location", "https://localhost/")
    }

    @Test
    fun `app root should redirect to login page using HTTPS given there is LB in front and request is using HTTPS`() {
        webClient.get().uri("http://localhost:$serverPort/")
            .header("X-Forwarded-Proto", "https")
            .exchange()
            .expectStatus().isFound
            .expectHeader().valueEquals("Location", "https://localhost/login")
    }

    @Test
    fun `login page should redirect to oauth provider given there is LB in front and request is using HTTPS`() {
        webClient.get().uri("http://localhost:$serverPort/login")
            .header("X-Forwarded-Proto", "https")
            .exchange()
            .expectStatus().isFound
            .expectHeader().valueMatches("Location", "https://localhost/oauth2/authorization/google")
    }

    @Test
    fun `list aliases page should redirect to login given the user is not authenticated`() {
        webClient.get().uri("/ui/alias")
            .exchange()
            .expectStatus().isFound
            .expectHeader().valueMatches("Location", "http://localhost:$serverPort/login")
    }

    @Test
    fun `link redirection endpoint should redirect users to login if user is not authorized`() {
        webClient.get().uri("/api/link/redirect?link=abc")
            .exchange()
            .expectStatus().isFound
            .expectHeader().valueMatches("Location", "http://localhost:$serverPort/login")
    }

    @Test
    fun `link suggestion endpoint should deny access if secret is not provided`() {
        webClient.get().uri("/api/link/suggest?link=abc")
            .exchange()
            .expectStatus().isUnauthorized
    }

    @RepeatedTest(2) // to test that cached data does not cause issues
    fun `link suggestion endpoint should allow access if secret is provided and valid`() {
        webClient.get().uri("/api/link/suggest?link=abc&secret=secret")
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun `actuator health endpoint should return 200 even if user is not authenticated`() {
        webClient.get().uri("/actuator/health")
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun `actuator metrics endpoint should return 401 if user is not authenticated`() {
        webClient.get().uri("/actuator/metrics")
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    fun `actuator metrics endpoint should return 200 if user is authenticated`() {
        webClient.get().uri("/actuator/metrics")
            .header("Authorization", "Basic ${ACTUATOR_USER_PASSWORD.toBase64()}")
            .exchange()
            .expectStatus().isOk
    }

    private fun String.toBase64() =
        Base64Utils.encodeToString(this.toByteArray(UTF_8))

    companion object {
        private const val ACTUATOR_USER_PASSWORD = "kotlinkactuator:kotlinkpass"
    }
}
