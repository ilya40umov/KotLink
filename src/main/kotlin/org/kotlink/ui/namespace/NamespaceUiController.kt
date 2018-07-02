package org.kotlink.ui.namespace

import mu.KLogging
import org.kotlink.core.namespace.Namespace
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
class NamespaceUiController(private val namespaceService: NamespaceService) {

    @GetMapping
    @SelectView(UiView.LIST_NAMESPACES)
    fun listNamespaces(model: Model): String {
        val namespaces = namespaceService.findAll()
        model.addAttribute("namespaces", namespaces)
        return "namespace/list"
    }

    @GetMapping("/new")
    fun newNamespace(model: Model): String {
        model.addAttribute("namespace", Namespace(keyword = ""))
        return "namespace/new"
    }

    @PostMapping("/new")
    fun createNamespace(
        @Valid @ModelAttribute("namespace") namespace: NamespaceUiValue,
        bindResult: BindingResult,
        model: Model,
        attributes: RedirectAttributes
    ): String {
        if (bindResult.hasErrors()) {
            logger.warn { "User input $namespace has failed validation ${bindResult.allErrors}" }
            model.addAttribute("namespace", namespace)
            return "namespace/new"
        }
        return try {
            val createdNamespace = namespaceService.create(namespace.toNamespace())
            attributes.addSuccessMessage("Namespace '${createdNamespace.keyword}' has been successfully created.")
            "redirect:/ui/namespace"
        } catch (e: Exception) {
            logger.error(e) { "Error occurred while creating a new namespace: $namespace" }
            model.addAttribute("namespace", namespace)
            model.addErrorMessage(
                "${e.javaClass.canonicalName} occurred (see logs for more details), message: ${e.message}")
            "namespace/new"
        }
    }

    @GetMapping("/{namespaceId}/edit")
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
        @Valid @ModelAttribute("namespace") namespace: NamespaceUiValue,
        bindResult: BindingResult,
        model: Model,
        attributes: RedirectAttributes
    ): String {
        if (bindResult.hasErrors()) {
            model.addAttribute("namespace", namespace)
            logger.warn { "User input $namespace has failed validation ${bindResult.allErrors}" }
            return "namespace/edit"
        }
        return try {
            namespaceService.update(namespace.apply { id = namespaceId }.toNamespace())
            attributes.addSuccessMessage("Namespace '${namespace.keyword}' has been successfully updated.")
            "redirect:/ui/namespace"
        } catch (e: Exception) {
            logger.error(e) { "Error occurred while updating an existing namespace: $namespace" }
            model.addAttribute("namespace", namespace)
            model.addErrorMessage(
                "${e.javaClass.canonicalName} occurred (see logs for more details), message: ${e.message}")
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
            attributes.addErrorMessage(
                "${e.javaClass.canonicalName} occurred (see logs for more details), message: ${e.message}")
            "redirect:/ui/namespace"
        }
    }

    companion object : KLogging()
}