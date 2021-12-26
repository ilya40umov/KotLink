package org.kotlink.ui.alias

import org.http4k.core.Response
import org.kotlink.domain.alias.Alias
import org.kotlink.domain.namespace.Namespace
import org.kotlink.framework.ui.FormErrors
import org.kotlink.framework.ui.ViewRenderer

fun ViewRenderer.aliasListView(aliases: List<Alias>): (Response) -> Response = doRender(
    template = "alias/list",
    data = mapOf("aliases" to aliases)
)

fun ViewRenderer.newAliasView(
    form: AliasForm,
    errors: FormErrors,
    namespaces: List<Namespace>,
): (Response) -> Response = doRender(
    template = "alias/new",
    data = mapOf(
        "form" to form,
        "errors" to errors,
        "namespaces" to namespaces
    )
)

fun ViewRenderer.editAliasView(
    form: AliasForm,
    errors: FormErrors,
    namespaces: List<Namespace>,
): (Response) -> Response = doRender(
    template = "alias/edit",
    data = mapOf(
        "form" to form,
        "errors" to errors,
        "namespaces" to namespaces
    )
)