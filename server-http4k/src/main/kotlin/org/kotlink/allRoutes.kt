package org.kotlink

import com.auth0.jwk.UrlJwkProvider
import org.http4k.client.ApacheClient
import org.http4k.core.Credentials
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.RequestContexts
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.then
import org.http4k.filter.ServerFilters.InitialiseRequestContext
import org.http4k.lens.RequestContextKey
import org.http4k.routing.ResourceLoader
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.routing.static
import org.http4k.security.OAuthProvider
import org.http4k.security.google
import org.kotlink.framework.crypto.AesEncryptionProvider
import org.kotlink.framework.oauth.CookieBasedOAuthPersistence
import org.kotlink.framework.oauth.IdTokenProcessor
import org.kotlink.framework.oauth.OAuthPrincipal
import org.kotlink.framework.oauth.OAuthPrincipalFilter
import org.kotlink.framework.thymeleaf.ThymeleafTemplateRenderer
import org.kotlink.ui.alias.aliasRoutes
import org.kotlink.ui.help.helpRoutes
import org.kotlink.ui.namespace.namespaceRoutes
import java.net.URL

fun allRoutes(config: KotLinkConfig): HttpHandler {
    val oAuthPersistence = CookieBasedOAuthPersistence(
        cookieNamePrefix = "Google",
        encryptionProvider = AesEncryptionProvider(encryptionKey = config.cookieEncryption.encryptionKey),
        idTokenProcessor = IdTokenProcessor(UrlJwkProvider(URL("https://www.googleapis.com/oauth2/v3/certs")))
    )
    val oauthProvider = OAuthProvider.google(
        client = ApacheClient(),
        credentials = Credentials(
            config.googleOAuth.googleClientId,
            config.googleOAuth.googleClientSecret
        ),
        callbackUri = config.googleOAuth.callbackUri,
        oAuthPersistence = oAuthPersistence,
        scopes = listOf("openid", "email", "profile")
    )

    val contexts = RequestContexts()
    val principal = RequestContextKey.required<OAuthPrincipal>(contexts)

    val templateRenderer = ThymeleafTemplateRenderer(config.hotReload)
    return routes(
        oauthProvider.callbackEndpoint,
        staticResources(config.hotReload),
        InitialiseRequestContext(contexts)
            .then(oauthProvider.authFilter)
            .then(OAuthPrincipalFilter(oAuthPersistence, principal))
            .then(
                routes(
                    "/" bind Method.GET to {
                        Response(Status.TEMPORARY_REDIRECT)
                            .header("Location", "/ui/alias")
                    },
                    "/ui/sign_out" bind Method.POST to {
                        oAuthPersistence.signOutResponse()
                    },
                    aliasRoutes(templateRenderer, principal),
                    namespaceRoutes(templateRenderer, principal),
                    helpRoutes(templateRenderer, principal)
                )
            )
    )
}

private fun staticResources(hotReload: Boolean): RoutingHttpHandler {
    val resourceLoader = if (hotReload) {
        ResourceLoader.Directory("${Constants.IDE_RESOURCES_DIRECTORY}/static")
    } else {
        ResourceLoader.Classpath("static")
    }
    return static(resourceLoader)
}