package org.kotlink.ui.alias

import mu.KLogging
import org.kotlink.core.alias.AliasService
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
@RequestMapping("/ui/alias")
class AliasUiController(
    private val aliasService: AliasService,
    private val namespaceService: NamespaceService
) {

    @GetMapping
    @SelectView(UiView.LIST_ALIASES)
    fun listAliases(model: Model): String {
        val aliases = aliasService.findAll()
        model.addAttribute("aliases", aliases)
        return "alias/list"
    }

    @GetMapping("/new")
    @SelectView(UiView.NEW_ALIAS)
    fun newAlias(model: Model): String {
        model.addAttribute("alias", AliasUiValue())
        model.addAttribute("namespaces", namespaceService.findAll())
        return "alias/new"
    }

    @PostMapping("/new")
    fun createAlias(
        @Valid @ModelAttribute("alias") alias: AliasUiValue,
        bindResult: BindingResult,
        model: Model,
        attributes: RedirectAttributes
    ): String {
        val namespace = namespaceService.findById(alias.namespaceId)
        if (bindResult.hasErrors() || namespace == null) {
            if (namespace == null) {
                bindResult.rejectValue("namespaceId", "", "namespace not found")
            }
            logger.warn { "User input $alias has failed validation ${bindResult.allErrors}" }
            model.addAttribute("alias", alias)
            model.addAttribute("namespaces", namespaceService.findAll())
            return "alias/new"
        }
        return try {
            val createdAlias = aliasService.create(alias.toAlias(namespace))
            attributes.addSuccessMessage("Alias '${createdAlias.fullLink}' has been successfully created.")
            "redirect:/ui/alias"
        } catch (e: Exception) {
            logger.error(e) { "Error occurred while creating a new alias: $alias" }
            model.addAttribute("alias", alias)
            model.addAttribute("namespaces", namespaceService.findAll())
            model.addErrorMessage(
                "${e.javaClass.canonicalName} occurred (see logs for more details), message: ${e.message}")
            "alias/new"
        }
    }

    @GetMapping("/{aliasId}/edit")
    @SelectView(UiView.EDIT_ALIAS)
    fun updateAlias(
        @PathVariable aliasId: Long,
        model: Model,
        attributes: RedirectAttributes
    ): String {
        val alias = aliasService.findById(aliasId)
        if (alias == null) {
            attributes.addErrorMessage("Alias #$aliasId was not found.")
            return "redirect:/ui/alias"
        }
        model.addAttribute("alias", AliasUiValue(alias))
        model.addAttribute("namespaces", namespaceService.findAll())
        return "alias/edit"
    }

    @PutMapping("/{aliasId}/edit")
    fun saveAlias(
        @PathVariable aliasId: Long,
        @Valid @ModelAttribute("alias") alias: AliasUiValue,
        bindResult: BindingResult,
        model: Model,
        attributes: RedirectAttributes
    ): String {
        val namespace = namespaceService.findById(alias.namespaceId)
        if (bindResult.hasErrors() || namespace == null) {
            if (namespace == null) {
                bindResult.rejectValue("namespaceId", "", "namespace not found")
            }
            model.addAttribute("alias", alias)
            model.addAttribute("namespaces", namespaceService.findAll())
            logger.warn { "User input $alias has failed validation ${bindResult.allErrors}" }
            return "alias/edit"
        }
        return try {
            val updatedAlias = aliasService.update(alias.toAlias(namespace))
            attributes.addSuccessMessage("Alias '${updatedAlias.fullLink}' has been successfully updated.")
            "redirect:/ui/alias"
        } catch (e: Exception) {
            logger.error(e) { "Error occurred while updating an existing alias: $alias" }
            model.addAttribute("alias", alias)
            model.addAttribute("namespaces", namespaceService.findAll())
            model.addErrorMessage(
                "${e.javaClass.canonicalName} occurred (see logs for more details), message: ${e.message}")
            "alias/edit"
        }
    }

    @DeleteMapping("/{aliasId}")
    fun deleteAlias(
        @PathVariable aliasId: Long,
        model: Model,
        attributes: RedirectAttributes
    ): String {
        return try {
            aliasService.deleteById(aliasId)
            attributes.addSuccessMessage("Alias #$aliasId has been successfully deleted.")
            "redirect:/ui/alias"
        } catch (e: Exception) {
            logger.error(e) { "Error occurred while deleting an alias: #$aliasId" }
            attributes.addErrorMessage(
                "${e.javaClass.canonicalName} occurred (see logs for more details), message: ${e.message}")
            "redirect:/ui/alias"
        }
    }

    companion object : KLogging()
}