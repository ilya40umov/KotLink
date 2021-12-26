package org.kotlink.ui.alias

import mu.KotlinLogging
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.body.form
import org.http4k.core.with
import org.http4k.lens.RequestContextLens
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.kotlink.domain.account.UserAccountService
import org.kotlink.domain.alias.AliasService
import org.kotlink.domain.namespace.NamespaceService
import org.kotlink.framework.exception.BadRequestException
import org.kotlink.framework.oauth.UserPrincipal
import org.kotlink.framework.ui.ViewRendererProvider
import org.kotlink.framework.ui.ViewUtils
import org.kotlink.framework.ui.idParamFromPath
import org.kotlink.framework.ui.searchParam
import org.kotlink.framework.ui.withErrorMessage
import org.kotlink.framework.ui.withSuccessMessage
import org.kotlink.ui.alias.AliasForm.Companion.toAliasForm

private val logger = KotlinLogging.logger {}

fun aliasRoutes(
    viewRenderer: ViewRendererProvider,
    principalLookup: RequestContextLens<UserPrincipal>,
    aliasService: AliasService,
    namespaceService: NamespaceService,
    userAccountService: UserAccountService
): RoutingHttpHandler {
    val aliasFormConverter = AliasFormConverter(aliasService, namespaceService, userAccountService)
    return routes(
        "/ui/alias" bind Method.GET to { request ->
            val aliases = aliasService.findContainingAllSearchKeywords(search = request.searchParam())
            Response(Status.OK).with(
                viewRenderer[request].aliasListView(aliases)
            )
        },
        "/ui/alias/new" bind Method.GET to { request ->
            val principal = principalLookup[request]
            Response(Status.OK).with(
                viewRenderer[request].newAliasView(
                    form = AliasForm.empty().copy(ownerEmail = principal.email),
                    errors = aliasFormConverter.getPotentialErrors(AliasForm.empty()),
                    namespaces = namespaceService.findAll()
                )
            )
        },
        "/ui/alias/new" bind Method.POST to { request ->
            val search = request.searchParam()
            val form = request.form().toAliasForm()
            try {
                val errorsOrAlias = aliasFormConverter.convertToAlias(form, isEdit = false)
                errorsOrAlias.fold({ errors ->
                    Response(Status.OK).with(
                        viewRenderer[request].newAliasView(
                            form = form,
                            errors = errors,
                            namespaces = namespaceService.findAll()
                        )
                    )
                }, { alias ->
                    aliasService.create(alias)
                    Response(Status.FOUND)
                        .header("Location", "/ui/alias?search=$search")
                        .withSuccessMessage("Alias ${ViewUtils.asPlainGoLink(alias)} has been successfully created.")
                })
            } catch (e: Exception) {
                logger.error(e) { "Failed to create the alias ${form}." }
                Response(Status.INTERNAL_SERVER_ERROR).with(
                    viewRenderer[request].newAliasView(
                        form = form,
                        errors = aliasFormConverter.getPotentialErrors(AliasForm.empty()),
                        namespaces = namespaceService.findAll()
                    )
                ).withErrorMessage("Failed to create the alias: ${e.message}")
            }
        },
        "/ui/alias/{id}/edit" bind Method.GET to { request ->
            val aliasId = request.idParamFromPath()
            val alias = aliasService.findByIdOrThrow(aliasId)
            val form = alias.toAliasForm()
            Response(Status.OK).with(
                viewRenderer[request].editAliasView(
                    form = form,
                    errors = aliasFormConverter.getPotentialErrors(AliasForm.empty()),
                    namespaces = namespaceService.findAll()
                )
            )
        },
        "/ui/alias/{id}/edit" bind Method.POST to { request ->
            val search = request.searchParam()
            val aliasId = request.idParamFromPath()
            val form = request.form().toAliasForm()
            try {
                val aliasFromDb = aliasService.findByIdOrThrow(aliasId)
                val errorsOrAlias = aliasFormConverter.convertToAlias(form, isEdit = true)
                errorsOrAlias.fold({ errors ->
                    Response(Status.OK).with(
                        viewRenderer[request].editAliasView(
                            form = form.copy(aliasId = aliasId),
                            errors = errors,
                            namespaces = namespaceService.findAll()
                        )
                    )
                }, { aliasFromClient ->
                    if (aliasFromClient.id != aliasFromDb.id) {
                        throw BadRequestException("Alias ID mismatch: '${aliasFromClient.id}' != '${aliasFromDb.id}'")
                    }
                    aliasService.update(aliasFromClient)
                    Response(Status.FOUND)
                        .header("Location", "/ui/alias?search=$search")
                        .withSuccessMessage(
                            "Alias ${ViewUtils.asPlainGoLink(aliasFromClient)} has been successfully updated."
                        )
                })
            } catch (e: Exception) {
                logger.error(e) { "Failed to update the alias ${aliasId}." }
                Response(Status.INTERNAL_SERVER_ERROR).with(
                    viewRenderer[request].editAliasView(
                        form = form,
                        errors = aliasFormConverter.getPotentialErrors(AliasForm.empty()),
                        namespaces = namespaceService.findAll()
                    )
                ).withErrorMessage("Failed to update the alias: ${e.message}")
            }
        },
        "/ui/alias/{id}/delete" bind Method.POST to { request ->
            val search = request.searchParam()
            val aliasId = request.idParamFromPath()
            val redirect = Response(Status.FOUND).header("Location", "/ui/alias?search=$search")
            try {
                val alias = aliasService.deleteById(aliasId)
                redirect.withSuccessMessage("Alias ${ViewUtils.asPlainGoLink(alias)} has been successfully deleted.")
            } catch (e: Exception) {
                logger.error(e) { "Failed to delete the alias ${aliasId}." }
                redirect.withErrorMessage("Failed to delete the alias: ${e.message}")
            }
        }
    )
}