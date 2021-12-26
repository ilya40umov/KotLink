package org.kotlink.ui.namespace

import org.http4k.core.Response
import org.kotlink.domain.namespace.Namespace
import org.kotlink.framework.ui.FormErrors
import org.kotlink.framework.ui.ViewRenderer

fun ViewRenderer.namespaceListView(
    namespaces: List<Namespace>
): (Response) -> Response = doRender(
    template = "namespace/list",
    data = mapOf("namespaces" to namespaces)
)

fun ViewRenderer.newNamespaceView(
    form: NamespaceForm,
    errors: FormErrors
): (Response) -> Response = doRender(
    template = "namespace/new",
    data = mapOf(
        "form" to form,
        "errors" to errors
    )
)

fun ViewRenderer.editNamespaceView(
    form: NamespaceForm,
    errors: FormErrors
): (Response) -> Response = doRender(
    template = "namespace/edit",
    data = mapOf(
        "form" to form,
        "errors" to errors
    )
)