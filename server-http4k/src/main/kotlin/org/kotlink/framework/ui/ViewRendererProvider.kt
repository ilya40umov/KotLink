package org.kotlink.framework.ui

import org.http4k.core.Request
import org.http4k.lens.RequestContextLens
import org.http4k.template.TemplateRenderer
import org.kotlink.framework.oauth.UserPrincipal

class ViewRendererProvider(
    private val templateRenderer: TemplateRenderer,
    private val principalLookup: RequestContextLens<UserPrincipal>
) {
    operator fun get(request: Request) = ViewRenderer(
        templateRenderer = templateRenderer,
        principalLookup = principalLookup,
        request = request
    )
}