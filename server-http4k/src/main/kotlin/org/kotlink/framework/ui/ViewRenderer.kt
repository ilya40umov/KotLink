package org.kotlink.framework.ui

import org.http4k.core.Body
import org.http4k.core.ContentType
import org.http4k.core.HttpMessage
import org.http4k.core.Request
import org.http4k.lens.LensFailure
import org.http4k.lens.RequestContextLens
import org.http4k.template.TemplateRenderer
import org.http4k.template.viewModel
import org.kotlink.framework.oauth.AnonymousUserPrincipal
import org.kotlink.framework.oauth.UserPrincipal

class ViewRenderer(
    private val templateRenderer: TemplateRenderer,
    private val principalLookup: RequestContextLens<UserPrincipal>,
    private val request: Request
) {
    fun <T : HttpMessage> doRender(
        template: String,
        data: Map<String, *>
    ): (T) -> T {
        val view = Body.viewModel(templateRenderer, ContentType.TEXT_HTML).toLens()
        return view of UiViewModel(
            template = template,
            data = data + extras()
        )
    }

    fun <T : HttpMessage> doRender400(): (T) -> T = doRender(
        template = "error/400",
        data = emptyMap<String, Any>() + extras()
    )

    fun <T : HttpMessage> doRender401(): (T) -> T = doRender(
        template = "error/401",
        data = emptyMap<String, Any>() + extras()
    )

    fun <T : HttpMessage> doRender404(): (T) -> T = doRender(
        template = "error/404",
        data = emptyMap<String, Any>() + extras()
    )

    fun <T : HttpMessage> doRender500(): (T) -> T = doRender(
        template = "error/500",
        data = emptyMap<String, Any>() + extras()
    )

    private fun extras(): Map<String, Any?> = mapOf(
        "principal" to principal(),
        "success_message" to request.successMessage(),
        "error_message" to request.errorMessage(),
        "search" to request.searchParam()
    )

    private fun principal(): UserPrincipal = try {
        principalLookup[request]
    } catch (lf: LensFailure) {
        AnonymousUserPrincipal
    }
}