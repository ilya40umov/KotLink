package org.kotlink.ui.alias

import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.body.form
import org.http4k.lens.RequestContextLens
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes
import org.http4k.template.TemplateRenderer
import org.kotlink.domain.account.UserAccountService
import org.kotlink.domain.alias.AliasService
import org.kotlink.domain.namespace.NamespaceService
import org.kotlink.framework.oauth.UserPrincipal
import org.kotlink.framework.exception.BadRequestException
import org.kotlink.framework.ui.renderView
import org.kotlink.ui.alias.AliasForm.Companion.toAliasForm

fun aliasRoutes(
    templateRenderer: TemplateRenderer,
    principal: RequestContextLens<UserPrincipal>,
    aliasService: AliasService,
    namespaceService: NamespaceService,
    userAccountService: UserAccountService
): RoutingHttpHandler {
    val aliasFormConverter = AliasFormConverter(aliasService, namespaceService, userAccountService)
    return routes(
        "/ui/alias" bind Method.GET to { request ->
            val search = request.searchParam()
            val aliases = aliasService.findContainingAllSearchKeywords(search)
            templateRenderer.renderView(
                template = "alias/list",
                principal = principal[request],
                data = mapOf(
                    "search" to search,
                    "aliases" to aliases
                )
            )
        },
        "/ui/alias/new" bind Method.GET to { request ->
            val search = request.query("search") ?: ""
            val user = principal[request]
            val emptyForm = AliasForm.empty()
            templateRenderer.renderView(
                template = "alias/new",
                principal = principal[request],
                data = mapOf(
                    "form" to emptyForm.copy(ownerEmail = user.email),
                    "errors" to aliasFormConverter.getPotentialErrors(emptyForm),
                    "namespaces" to namespaceService.findAll(),
                    "search" to search
                )
            )
        },
        "/ui/alias/new" bind Method.POST to { request ->
            val search = request.query("search") ?: ""
            val form = request.form().toAliasForm()
            val errorsOrAlias = aliasFormConverter.convertToAlias(form, isEdit = false)
            errorsOrAlias.fold({ errors ->
                templateRenderer.renderView(
                    template = "alias/new",
                    principal = principal[request],
                    data = mapOf(
                        "form" to form,
                        "errors" to errors,
                        "namespaces" to namespaceService.findAll(),
                        "search" to search
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
            val aliasId = request.aliasIdParam()
            val alias = aliasService.findByIdOrThrow(aliasId)
            val form = alias.toAliasForm()
            templateRenderer.renderView(
                template = "alias/edit",
                principal = principal[request],
                data = mapOf(
                    "form" to form,
                    "errors" to aliasFormConverter.getPotentialErrors(AliasForm.empty()),
                    "namespaces" to namespaceService.findAll(),
                    "search" to search
                )
            )
        },
        "/ui/alias/{id}/edit" bind Method.POST to { request ->
            val search = request.query("search") ?: ""
            val aliasId = request.aliasIdParam()
            val aliasFromDb = aliasService.findByIdOrThrow(aliasId)
            val form = request.form().toAliasForm()
            val errorsOrAlias = aliasFormConverter.convertToAlias(form, isEdit = true)
            errorsOrAlias.fold({ errors ->
                templateRenderer.renderView(
                    template = "alias/edit",
                    principal = principal[request],
                    data = mapOf(
                        "form" to form.copy(aliasId = aliasId),
                        "errors" to errors,
                        "namespaces" to namespaceService.findAll(),
                        "search" to search
                    )
                )
            }, { aliasFromClient ->
                if (aliasFromClient.id != aliasFromDb.id) {
                    throw BadRequestException("Alias ID mismatch: '${aliasFromClient.id}' != '${aliasFromDb.id}'")
                }
                aliasService.update(aliasFromClient)
                Response(Status.FOUND)
                    .header("Location", "/ui/alias?search=$search")
            })
        },
        "/ui/alias/{id}/delete" bind Method.POST to { request ->
            val search = request.searchParam()
            val aliasId = request.aliasIdParam()
            aliasService.deleteById(aliasId)
            Response(Status.FOUND)
                .header("Location", "/ui/alias?search=$search")
        }
    )
}

private fun Request.searchParam(): String {
    val rawValue = query("search") ?: ""
    return rawValue.lowercase().replace("[^a-z0-9\\s]+".toRegex(), " ")
}

private fun Request.aliasIdParam(): String {
    val rawValue = path("id") ?: throw IllegalStateException("No alias ID in the path")
    return rawValue.lowercase().replace("[^a-z0-9_]+".toRegex(), "_")
}