package org.kotlink.ui.namespace

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
import org.kotlink.domain.namespace.NamespaceService
import org.kotlink.framework.exception.BadRequestException
import org.kotlink.framework.oauth.UserPrincipal
import org.kotlink.framework.ui.ViewRendererProvider
import org.kotlink.framework.ui.idParamFromPath
import org.kotlink.framework.ui.searchParam
import org.kotlink.framework.ui.withErrorMessage
import org.kotlink.framework.ui.withSuccessMessage
import org.kotlink.ui.namespace.NamespaceForm.Companion.toNamespaceForm

private val logger = KotlinLogging.logger {}

fun namespaceRoutes(
    viewRenderer: ViewRendererProvider,
    principalLookup: RequestContextLens<UserPrincipal>,
    namespaceService: NamespaceService,
    userAccountService: UserAccountService
): RoutingHttpHandler {
    val namespaceFormConverter = NamespaceFormConverter(namespaceService, userAccountService)
    return routes(
        "/ui/namespace" bind Method.GET to { request ->
            Response(Status.OK).with(
                viewRenderer[request].namespaceListView(namespaces = namespaceService.findAll())
            )
        },
        "/ui/namespace/new" bind Method.GET to { request ->
            val principal = principalLookup[request]
            Response(Status.OK).with(
                viewRenderer[request].newNamespaceView(
                    form = NamespaceForm.empty().copy(ownerEmail = principal.email),
                    errors = namespaceFormConverter.getPotentialErrors(NamespaceForm.empty())
                )
            )
        },
        "/ui/namespace/new" bind Method.POST to { request ->
            val form = request.form().toNamespaceForm()
            try {
                val errorsOrNamespace = namespaceFormConverter.convertToNamespace(form, isEdit = false)
                errorsOrNamespace.fold({ errors ->
                    Response(Status.OK).with(
                        viewRenderer[request].newNamespaceView(
                            form = form,
                            errors = errors
                        )
                    )
                }, { namespace ->
                    namespaceService.create(namespace)
                    Response(Status.FOUND)
                        .header("Location", "/ui/namespace")
                        .withSuccessMessage("Namespace ${namespace.linkPrefix} has been successfully created.")
                })
            } catch (e: Exception) {
                logger.error(e) { "Failed to create the namespace ${form}." }
                Response(Status.INTERNAL_SERVER_ERROR).with(
                    viewRenderer[request].newNamespaceView(
                        form = form,
                        errors = namespaceFormConverter.getPotentialErrors(NamespaceForm.empty())
                    )
                ).withErrorMessage("Failed to create the namespace: ${e.message}")
            }
        },
        "/ui/namespace/{id}/edit" bind Method.GET to { request ->
            val namespaceId = request.idParamFromPath()
            val namespace = namespaceService.findByIdOrThrow(namespaceId)
            val form = namespace.toNamespaceForm()
            Response(Status.OK).with(
                viewRenderer[request].editNamespaceView(
                    form = form,
                    errors = namespaceFormConverter.getPotentialErrors(NamespaceForm.empty())
                )
            )
        },
        "/ui/namespace/{id}/edit" bind Method.POST to { request ->
            val search = request.searchParam()
            val namespaceId = request.idParamFromPath()
            val form = request.form().toNamespaceForm()
            try {
                val namespaceFromDb = namespaceService.findByIdOrThrow(namespaceId)
                val errorsOrNamespace = namespaceFormConverter.convertToNamespace(form, isEdit = true)
                errorsOrNamespace.fold({ errors ->
                    Response(Status.OK).with(
                        viewRenderer[request].editNamespaceView(
                            form = form.copy(namespaceId = namespaceId),
                            errors = errors
                        )
                    )
                }, { namespaceFromClient ->
                    if (namespaceFromClient.id != namespaceFromDb.id) {
                        throw BadRequestException(
                            "Namespace ID mismatch: '${namespaceFromClient.id}' != '${namespaceFromDb.id}'"
                        )
                    }
                    namespaceService.update(namespaceFromClient)
                    Response(Status.FOUND)
                        .header("Location", "/ui/namespace?search=$search")
                        .withSuccessMessage(
                            "Namespace ${namespaceFromClient.linkPrefix} has been successfully updated."
                        )
                })
            } catch (e: Exception) {
                logger.error(e) { "Failed to update the namespace ${namespaceId}." }
                Response(Status.INTERNAL_SERVER_ERROR).with(
                    viewRenderer[request].editNamespaceView(
                        form = form,
                        errors = namespaceFormConverter.getPotentialErrors(NamespaceForm.empty())
                    )
                ).withErrorMessage("Failed to update the namespace: ${e.message}")
            }
        },
        "/ui/namespace/{id}/delete" bind Method.POST to { request ->
            val namespaceId = request.idParamFromPath()
            val redirect = Response(Status.FOUND).header("Location", "/ui/namespace")
            try {
                val namespace = namespaceService.deleteById(namespaceId)
                redirect.withSuccessMessage("Namespace ${namespace.linkPrefix} has been successfully deleted.")
            } catch (e: Exception) {
                logger.error(e) { "Failed to delete the namespace ${namespaceId}." }
                redirect.withErrorMessage("Failed to delete the namespace: ${e.message}")
            }
        }
    )
}