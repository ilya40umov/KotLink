package org.kotlink

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.web.server.LocalServerPort

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
class KotLinkSecurityAndSslTest {

    @Autowired
    private lateinit var webClient: WebTestClient

    @LocalServerPort
    private var serverPort: Int = 8080

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
            .expectHeader().valueMatches("Location", "https://accounts.google.com.*")
    }

    @Test
    fun `actuator health endpoint should return 200 even if user is not authenticated`() {
        webClient.get().uri("/actuator/health")
            .exchange()
            .expectStatus().isOk
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
}
