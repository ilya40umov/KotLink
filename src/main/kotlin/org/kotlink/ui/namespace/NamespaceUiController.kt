package org.kotlink.ui.namespace

import mu.KLogging
import org.kotlink.core.CurrentUser
import org.kotlink.core.namespace.KeywordTakenException
import org.kotlink.core.namespace.NamespaceService
import org.kotlink.ui.SelectView
import org.kotlink.ui.UiView
import org.kotlink.ui.addErrorMessage
import org.kotlink.ui.addSuccessMessage
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import javax.validation.Valid

@Controller
@RequestMapping("/ui/namespace")
@Suppress("TooGenericExceptionCaught")
class NamespaceUiController(
    private val namespaceService: NamespaceService,
    private val namespaceUiValueConverter: NamespaceUiValueConverter,
    private val currentUser: CurrentUser
) {

    @GetMapping
    @SelectView(UiView.LIST_NAMESPACES)
    fun listNamespaces(model: Model): String {
        val namespaces = namespaceService.findAll()
        model.addAttribute("namespaces", namespaces)
        return "namespace/list"
    }

    @GetMapping("/new")
    @SelectView(UiView.NEW_NAMESPACE)
    fun newNamespace(model: Model): String {
        model.addAttribute("namespace", NamespaceUiValue().apply {
            ownerAccountEmail = currentUser.getEmail()
        })
        return "namespace/new"
    }

    @PostMapping("/new")
    fun createNamespace(
        @Valid @ModelAttribute("namespace") namespaceUiValue: NamespaceUiValue,
        bindResult: BindingResult,
        model: Model,
        attributes: RedirectAttributes
    ): String {
        if (bindResult.hasErrors()) {
            logger.warn { "User input $namespaceUiValue has failed validation ${bindResult.allErrors}" }
            model.addAttribute("namespace", namespaceUiValue)
            return "namespace/new"
        }
        return try {
            val namespace = namespaceUiValueConverter.convertValueToModel(namespaceUiValue)
            val createdNamespace = namespaceService.create(namespace)
            attributes.addSuccessMessage("Namespace '${createdNamespace.keyword}' has been successfully created.")
            "redirect:/ui/namespace"
        } catch (e: KeywordTakenException) {
            bindResult.rejectValue("keyword", "", "keyword is taken")
            model.addAttribute("namespace", namespaceUiValue)
            "namespace/new"
        } catch (e: Exception) {
            logger.error(e) { "Error occurred while creating a new namespace: $namespaceUiValue" }
            model.addAttribute("namespace", namespaceUiValue)
            model.addErrorMessage(e)
            "namespace/new"
        }
    }

    @GetMapping("/{namespaceId}/edit")
    @SelectView(UiView.EDIT_NAMESPACE)
    fun updateNamespace(
        @PathVariable namespaceId: Long,
        model: Model,
        attributes: RedirectAttributes
    ): String {
        val namespace = namespaceService.findById(namespaceId)
        if (namespace == null) {
            attributes.addErrorMessage("Namespace #$namespaceId was not found.")
            return "redirect:/ui/namespace"
        }
        model.addAttribute("namespace", NamespaceUiValue(namespace))
        return "namespace/edit"
    }

    @PutMapping("/{namespaceId}/edit")
    fun saveNamespace(
        @PathVariable namespaceId: Long,
        @Valid @ModelAttribute("namespace") namespaceUiValue: NamespaceUiValue,
        bindResult: BindingResult,
        model: Model,
        attributes: RedirectAttributes
    ): String {
        if (bindResult.hasErrors()) {
            model.addAttribute("namespace", namespaceUiValue)
            logger.warn { "User input $namespaceUiValue has failed validation ${bindResult.allErrors}" }
            return "namespace/edit"
        }
        return try {
            val namespace = namespaceUiValueConverter.convertValueToModel(
                namespaceUiValue.apply {
                    id = namespaceId
                }
            )
            namespaceService.update(namespace)
            attributes.addSuccessMessage("Namespace '${namespaceUiValue.keyword}' has been successfully updated.")
            "redirect:/ui/namespace"
        } catch (e: KeywordTakenException) {
            bindResult.rejectValue("keyword", "", "keyword is taken")
            model.addAttribute("namespace", namespaceUiValue)
            "namespace/edit"
        } catch (e: Exception) {
            logger.error(e) { "Error occurred while updating an existing namespace: $namespaceUiValue" }
            model.addAttribute("namespace", namespaceUiValue)
            model.addErrorMessage(e)
            "namespace/edit"
        }
    }

    @DeleteMapping("/{namespaceId}")
    fun deleteNamespace(
        @PathVariable namespaceId: Long,
        model: Model,
        attributes: RedirectAttributes
    ): String {
        return try {
            namespaceService.deleteById(namespaceId)
            attributes.addSuccessMessage("Namespace #$namespaceId has been successfully deleted.")
            "redirect:/ui/namespace"
        } catch (e: Exception) {
            logger.error(e) { "Error occurred while deleting a namespace: #$namespaceId" }
            attributes.addErrorMessage(e)
            "redirect:/ui/namespace"
        }
    }

    companion object : KLogging()
}