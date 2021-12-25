package org.kotlink.framework.oauth

import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import org.http4k.core.with
import org.http4k.lens.RequestContextLens
import org.kotlink.domain.account.UserAccountService

class OAuthUserPrincipalFilter(
    private val oAuthPersistence: CookieBasedOAuthPersistence,
    private val principal: RequestContextLens<UserPrincipal>,
    private val userAccountService: UserAccountService
) : Filter {
    override fun invoke(next: HttpHandler): HttpHandler {
        return { request ->
            val userEmail = oAuthPersistence.retrieveUserEmail(request)
            if (userEmail == null || userEmail != "illia.sorokoumov@gmail.com") {
                oAuthPersistence.authFailureResponse()
            } else {
                userAccountService.findOrCreateAccountForEmail(userEmail)
                next(request.with(principal of OAuthUserPrincipal(email = userEmail)))
            }
        }
    }
}