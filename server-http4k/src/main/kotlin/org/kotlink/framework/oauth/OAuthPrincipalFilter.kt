package org.kotlink.framework.oauth

import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import org.http4k.core.with
import org.http4k.lens.RequestContextLens
import org.kotlink.domain.account.UserAccountService

class OAuthPrincipalFilter(
    private val oAuthPersistence: CookieBasedOAuthPersistence,
    private val oAuthPrincipal: RequestContextLens<OAuthPrincipal>,
    private val userAccountService: UserAccountService
) : Filter {
    override fun invoke(next: HttpHandler): HttpHandler {
        return { request ->
            val userEmail = oAuthPersistence.retrieveUserEmail(request)
            if (userEmail == null) {
                // TODO handle this case properly
                oAuthPersistence.authFailureResponse()
            } else {
                userAccountService.findOrCreateAccountForEmail(userEmail)
                next(request.with(oAuthPrincipal of OAuthPrincipal(email = userEmail)))
            }
        }
    }
}