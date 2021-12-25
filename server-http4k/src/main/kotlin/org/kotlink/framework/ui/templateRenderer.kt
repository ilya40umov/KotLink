package org.kotlink.framework.ui

import org.http4k.core.Body
import org.http4k.core.ContentType
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.template.TemplateRenderer
import org.http4k.template.viewModel
import org.kotlink.framework.oauth.UserPrincipal

fun TemplateRenderer.renderView(
    status: Status = Status.OK,
    template: String,
    principal: UserPrincipal,
    data: Map<String, *>
): Response {
    val view = Body.viewModel(this, ContentType.TEXT_HTML).toLens()
    return Response(status).with(
        view of UiViewModel(
            template = template,
            principal = principal,
            data = data
        )
    )
}

fun TemplateRenderer.render400(
    principal: UserPrincipal
) = renderView(
    status = Status.BAD_REQUEST,
    template = "error/400",
    principal = principal,
    data = emptyMap<String, Any>()
)

fun TemplateRenderer.render404(
    principal: UserPrincipal
) = renderView(
    status = Status.NOT_FOUND,
    template = "error/404",
    principal = principal,
    data = emptyMap<String, Any>()
)

fun TemplateRenderer.render500(
    principal: UserPrincipal
) = renderView(
    status = Status.INTERNAL_SERVER_ERROR,
    template = "error/500",
    principal = principal,
    data = emptyMap<String, Any>()
)