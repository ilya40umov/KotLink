package org.kotlink.ui.alias

import org.http4k.core.Body
import org.http4k.core.ContentType
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.body.form
import org.http4k.core.with
import org.http4k.lens.RequestContextLens
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes
import org.http4k.template.TemplateRenderer
import org.http4k.template.viewModel
import org.kotlink.domain.account.UserAccountService
import org.kotlink.domain.alias.AliasService
import org.kotlink.domain.namespace.NamespaceService
import org.kotlink.framework.oauth.OAuthPrincipal
import org.kotlink.framework.ui.UiViewModel.Companion.uiViewModel
import org.kotlink.ui.alias.AliasForm.Companion.toAliasForm
import java.lang.IllegalArgumentException

fun aliasRoutes(
    templateRenderer: TemplateRenderer,
    principal: RequestContextLens<OAuthPrincipal>,
    aliasService: AliasService,
    namespaceService: NamespaceService,
    userAccountService: UserAccountService
): RoutingHttpHandler {
    val aliasFormConverter = AliasFormConverter(aliasService, namespaceService, userAccountService)
    return routes(
        "/ui/alias" bind Method.GET to { request ->
            val search = request.query("search") ?: ""
            val aliases = aliasService.findAliasesWithFullLinkMatchingEntireInput(userProvidedInput = search)
            val view = Body.viewModel(templateRenderer, ContentType.TEXT_HTML).toLens()
            Response(Status.OK).with(
                view of request.uiViewModel(
                    template = "alias/list",
                    data = mapOf(
                        "search" to search,
                        "aliases" to aliases
                    ),
                    principal = principal
                )
            )
        },
        "/ui/alias/new" bind Method.GET to { request ->
            val search = request.query("search") ?: ""
            val user = principal[request]
            val emptyForm = AliasForm.empty()
            val view = Body.viewModel(templateRenderer, ContentType.TEXT_HTML).toLens()
            Response(Status.OK).with(
                view of request.uiViewModel(
                    template = "alias/new",
                    data = mapOf(
                        "form" to emptyForm.copy(ownerEmail = user.email),
                        "errors" to aliasFormConverter.getPotentialErrors(emptyForm),
                        "namespaces" to namespaceService.findAll(),
                        "search" to search
                    ),
                    principal = principal
                )
            )
        },
        "/ui/alias/new" bind Method.POST to { request ->
            val search = request.query("search") ?: ""
            val form = request.form().toAliasForm()
            val errorsOrAlias = aliasFormConverter.convertToAlias(form, isEdit = false)
            errorsOrAlias.fold({ errors ->
                val view = Body.viewModel(templateRenderer, ContentType.TEXT_HTML).toLens()
                Response(Status.OK).with(
                    view of request.uiViewModel(
                        template = "alias/new",
                        data = mapOf(
                            "form" to form,
                            "errors" to errors,
                            "namespaces" to namespaceService.findAll(),
                            "search" to search
                        ),
                        principal = principal
                    )
                )
            }, { alias ->
                aliasService.create(alias)
                Response(Status.FOUND)
                    .header("Location", "/ui/alias?search=$search")
            })
        },
        "/ui/alias/{id}/edit" bind Method.GET to { request ->
            val search = request.query("search") ?: ""
            val aliasId = request.path("id") ?: throw IllegalArgumentException("No alias ID provided")
            val alias = aliasService.findById(aliasId) ?: throw IllegalArgumentException("Alias not found")
            val form = alias.toAliasForm()
            val view = Body.viewModel(templateRenderer, ContentType.TEXT_HTML).toLens()
            Response(Status.OK).with(
                view of request.uiViewModel(
                    template = "alias/edit",
                    data = mapOf(
                        "form" to form,
                        "errors" to aliasFormConverter.getPotentialErrors(AliasForm.empty()),
                        "namespaces" to namespaceService.findAll(),
                        "search" to search
                    ),
                    principal = principal
                )
            )
        },
        "/ui/alias/{id}/edit" bind Method.POST to { request ->
            val search = request.query("search") ?: ""
            val aliasId = request.path("id") ?: throw IllegalArgumentException("No alias ID provided")
            val foundAlias = aliasService.findById(aliasId) ?: throw IllegalArgumentException("Alias not found")
            val form = request.form().toAliasForm()
            val errorsOrAlias = aliasFormConverter.convertToAlias(form, isEdit = true)
            errorsOrAlias.fold({ errors ->
                val view = Body.viewModel(templateRenderer, ContentType.TEXT_HTML).toLens()
                Response(Status.OK).with(
                    view of request.uiViewModel(
                        template = "alias/edit",
                        data = mapOf(
                            "form" to form.copy(aliasId = aliasId),
                            "errors" to errors,
                            "namespaces" to namespaceService.findAll(),
                            "search" to search
                        ),
                        principal = principal
                    )
                )
            }, { alias ->
                if (alias.id != foundAlias.id) {
                    throw IllegalArgumentException("Alias ID mismatch")
                }
                aliasService.update(alias)
                Response(Status.FOUND)
                    .header("Location", "/ui/alias?search=$search")
            })
        },
        "/ui/alias/{id}/delete" bind Method.POST to { request ->
            val search = request.query("search") ?: ""
            val aliasId = request.path("id") ?: throw IllegalArgumentException("No alias ID provided")
            aliasService.deleteById(aliasId)
            Response(Status.FOUND)
                .header("Location", "/ui/alias?search=$search")
        }
    )
}