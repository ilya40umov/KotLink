package org.kotlink.framework.oauth

import mu.KotlinLogging
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Uri
import org.http4k.core.cookie.Cookie
import org.http4k.core.cookie.cookie
import org.http4k.core.cookie.invalidate
import org.http4k.core.cookie.replaceCookie
import org.http4k.security.AccessToken
import org.http4k.security.CrossSiteRequestForgeryToken
import org.http4k.security.Nonce
import org.http4k.security.OAuthPersistence
import org.http4k.security.openid.IdToken
import org.kotlink.framework.crypto.EncryptionProvider
import java.security.GeneralSecurityException
import java.time.Clock
import java.time.Duration

private val logger = KotlinLogging.logger {}

class CookieBasedOAuthPersistence(
    cookieNamePrefix: String,
    private val idTokenProcessor: IdTokenProcessor,
    private val encryptionProvider: EncryptionProvider,
    private val cookiePath: String = "/",
    private val cookieValidity: Duration = Duration.ofHours(DEFAULT_COOKIE_VALIDITY_HOURS),
    private val clock: Clock = Clock.systemDefaultZone()
) : OAuthPersistence {

    private val csrfCookie = "${cookieNamePrefix}Csrf"
    private val nonceCookie = "${cookieNamePrefix}Nonce"
    private val originalUriCookie = "${cookieNamePrefix}OriginalUri"
    private val accessTokenCookie = "${cookieNamePrefix}AccessToken"
    private val emailUserCookie = "${cookieNamePrefix}Email"

    override fun retrieveCsrf(request: Request) =
        request.decryptedCookieValue(csrfCookie)?.let(::CrossSiteRequestForgeryToken)

    override fun retrieveToken(request: Request): AccessToken? =
        request.decryptedCookieValue(accessTokenCookie)?.let(::AccessToken)

    override fun retrieveNonce(request: Request): Nonce? =
        request.decryptedCookieValue(nonceCookie)?.let(::Nonce)

    override fun retrieveOriginalUri(request: Request): Uri? =
        request.decryptedCookieValue(originalUriCookie)?.let(Uri::of)

    fun retrieveUserEmail(request: Request): String? =
        request.decryptedCookieValue(emailUserCookie)

    override fun assignCsrf(redirect: Response, csrf: CrossSiteRequestForgeryToken) =
        redirect.encryptedExpiringCookie(csrfCookie, csrf.value)

    override fun assignToken(
        request: Request,
        redirect: Response,
        accessToken: AccessToken,
        idToken: IdToken?
    ): Response {
        if (idToken == null) {
            return authFailureResponse()
        }
        return redirect
            .encryptedExpiringCookie(accessTokenCookie, accessToken.value)
            .encryptedExpiringCookie(emailUserCookie, idTokenProcessor.extractEmail(idToken))
            .invalidateCookie(csrfCookie)
            .invalidateCookie(nonceCookie)
            .invalidateCookie(originalUriCookie)
    }

    override fun assignNonce(redirect: Response, nonce: Nonce): Response =
        redirect.encryptedExpiringCookie(nonceCookie, nonce.value)

    override fun assignOriginalUri(redirect: Response, originalUri: Uri): Response =
        redirect.encryptedExpiringCookie(originalUriCookie, originalUri.toString())

    override fun authFailureResponse() =
        Response(Status.FORBIDDEN)
            .invalidateCookie(csrfCookie)
            .invalidateCookie(accessTokenCookie)
            .invalidateCookie(nonceCookie)
            .invalidateCookie(originalUriCookie)
            .invalidateCookie(emailUserCookie)

    fun signOutResponse(redirectTo: String = "/ui/alias") =
        Response(Status.FOUND)
            .invalidateCookie(csrfCookie)
            .invalidateCookie(accessTokenCookie)
            .invalidateCookie(nonceCookie)
            .invalidateCookie(originalUriCookie)
            .invalidateCookie(emailUserCookie)
            .header("Location", redirectTo)

    private fun Response.invalidateCookie(name: String): Response =
        replaceCookie(Cookie(name = name, value = "", path = cookiePath).invalidate())

    private fun Request.decryptedCookieValue(name: String): String? =
        cookie(name)?.value?.let { value ->
            try {
                encryptionProvider.decrypt(value)
            } catch (e: GeneralSecurityException) {
                logger.warn(e) { "Couldn't decrypt cookie '$name' with value '$value'" }
                null
            }
        }

    private fun Response.encryptedExpiringCookie(name: String, value: String): Response =
        cookie(expiring(name, encryptionProvider.encrypt(value)))

    private fun expiring(name: String, value: String) = Cookie(
        name = name,
        value = value,
        expires = clock.instant().plus(cookieValidity),
        path = cookiePath
    )

    companion object {
        private const val DEFAULT_COOKIE_VALIDITY_HOURS = 12L
    }
}