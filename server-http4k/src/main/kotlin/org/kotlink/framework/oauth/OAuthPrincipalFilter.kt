package org.kotlink.framework.oauth

import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import org.http4k.core.with
import org.http4k.lens.RequestContextLens

class OAuthPrincipalFilter(
    private val oAuthPersistence: CookieBasedOAuthPersistence,
    private val principal: RequestContextLens<OAuthPrincipal>
) : Filter {
    override fun invoke(next: HttpHandler): HttpHandler {
        return { request ->
            val userEmail = oAuthPersistence.retrieveUserEmail(request)
            if (userEmail == null) {
                // TODO handle this case properly
                oAuthPersistence.authFailureResponse()
            } else {
                next(request.with(principal of OAuthPrincipal(email = userEmail)))
            }
        }
    }
}