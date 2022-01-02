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
import org.http4k.core.with
import org.http4k.filter.FlashAttributesFilter
import org.http4k.filter.ServerFilters.InitialiseRequestContext
import org.http4k.lens.RequestContextKey
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.security.OAuthProvider
import org.http4k.security.google
import org.kotlink.api.autocompleteRoute
import org.kotlink.api.redirectRoute
import org.kotlink.domain.account.UserAccountRepository
import org.kotlink.domain.account.UserAccountService
import org.kotlink.domain.alias.AliasRepository
import org.kotlink.domain.alias.AliasService
import org.kotlink.domain.alias.FullLinkRepository
import org.kotlink.domain.namespace.NamespaceRepository
import org.kotlink.domain.namespace.NamespaceService
import org.kotlink.framework.crypto.AesEncryptionProvider
import org.kotlink.framework.oauth.CookieBasedOAuthPersistence
import org.kotlink.framework.oauth.IdTokenProcessor
import org.kotlink.framework.oauth.OAuthErrorHandlingFilter
import org.kotlink.framework.oauth.OAuthUserPrincipalFilter
import org.kotlink.framework.oauth.UserPrincipal
import org.kotlink.framework.ui.ThymeleafTemplateRenderer
import org.kotlink.framework.ui.UiErrorHandlingFilter
import org.kotlink.framework.ui.ViewRendererProvider
import org.kotlink.framework.ui.staticResources
import org.kotlink.ui.alias.aliasRoutes
import org.kotlink.ui.help.helpRoutes
import org.kotlink.ui.namespace.namespaceRoutes
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import java.net.URI
import java.net.URL

fun allRoutes(config: KotLinkConfig): HttpHandler {
    val dynamoDbClient = DynamoDbClient.builder()
        .apply {
            if (config.dynamoDb.endpointOverride != null) {
                endpointOverride(URI(config.dynamoDb.endpointOverride.toString()))
                credentialsProvider(AnonymousCredentialsProvider.create())
            }
        }
        .build()

    val aliasService = AliasService(
        aliasRepository = AliasRepository(dbClient = dynamoDbClient, tableName = config.dynamoDb.tableName),
        fullLinkRepository = FullLinkRepository(dbClient = dynamoDbClient, tableName = config.dynamoDb.tableName)
    )
    val namespaceService = NamespaceService(
        NamespaceRepository(dbClient = dynamoDbClient, tableName = config.dynamoDb.tableName)
    )
    val userAccountService = UserAccountService(
        UserAccountRepository(dbClient = dynamoDbClient, tableName = config.dynamoDb.tableName)
    )

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
    val principalLookup = RequestContextKey.required<UserPrincipal>(contexts)

    val viewRenderer = ViewRendererProvider(
        templateRenderer = ThymeleafTemplateRenderer(config.hotReload),
        principalLookup = principalLookup
    )
    return routes(
        oauthProvider.callbackEndpoint,
        staticResources(config.hotReload),
        autocompleteRoute(aliasService),
        InitialiseRequestContext(contexts)
            .then(OAuthErrorHandlingFilter(viewRenderer))
            .then(oauthProvider.authFilter)
            .then(OAuthUserPrincipalFilter(oAuthPersistence, principalLookup, userAccountService))
            .then(UiErrorHandlingFilter(viewRenderer))
            .then(FlashAttributesFilter)
            .then(
                routes(
                    aliasRoutes(viewRenderer, principalLookup, aliasService, namespaceService, userAccountService),
                    namespaceRoutes(viewRenderer, principalLookup, namespaceService, userAccountService),
                    helpRoutes(viewRenderer),
                    redirectRoute(aliasService),
                    "/" bind Method.GET to {
                        Response(Status.TEMPORARY_REDIRECT)
                            .header("Location", "/ui/alias")
                    },
                    "/ui/sign_out" bind Method.POST to {
                        oAuthPersistence.signOutResponse()
                    },
                    "{path:.*}" bind Method.GET to { request ->
                        Response(Status.NOT_FOUND).with(
                            viewRenderer[request].doRender(
                                template = "error/404",
                                data = emptyMap<String, Any>()
                            )
                        )
                    }
                )
            )
    )
}