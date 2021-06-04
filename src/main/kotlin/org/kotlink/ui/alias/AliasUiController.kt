package org.kotlink.ui.alias

import mu.KLogging
import org.kotlink.core.CurrentUser
import org.kotlink.core.alias.AliasService
import org.kotlink.core.alias.FullLinkExistsException
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
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import javax.validation.Valid

@Controller
@RequestMapping("/ui/alias")
@Suppress("TooGenericExceptionCaught")
class AliasUiController(
    private val aliasService: AliasService,
    private val namespaceService: NamespaceService,
    private val aliasUiValueConverter: AliasUiValueConverter,
    private val currentUser: CurrentUser
) {

    @GetMapping
    @SelectView(UiView.LIST_ALIASES)
    fun listAliases(
        @RequestParam(name = "input", defaultValue = "") input: String,
        @RequestParam(defaultValue = "0") offset: Int,
        @RequestParam(defaultValue = "25") limit: Int,
        model: Model
    ): String {
        val aliases = aliasService.findAliasesWithFullLinkMatchingEntireInput(
            userProvidedInput = input,
            offset = offset,
            limit = limit
        )
        model.addAttribute("aliases", aliases)
        return "alias/list"
    }

    @GetMapping("/new")
    @SelectView(UiView.NEW_ALIAS)
    fun newAlias(model: Model): String {
        model.addFormAttributes(AliasUiValue()
            .apply { ownerAccountEmail = currentUser.getEmail() })
        return "alias/new"
    }

    @PostMapping("/new")
    fun createAlias(
        @Valid @ModelAttribute("alias") aliasUiValue: AliasUiValue,
        bindResult: BindingResult,
        model: Model,
        attributes: RedirectAttributes
    ): String {
        val namespace = namespaceService.findById(aliasUiValue.namespaceId)
        if (bindResult.hasErrors() || namespace == null) {
            if (namespace == null) {
                bindResult.rejectValue("namespaceId", "", "namespace not found")
            }
            logger.warn { "User input $aliasUiValue has failed validation ${bindResult.allErrors}" }
            model.addFormAttributes(aliasUiValue)
            return "alias/new"
        }
        return try {
            val alias = aliasUiValueConverter.convertValueToModel(aliasUiValue, namespace)
            val createdAlias = aliasService.create(alias)
            attributes.addSuccessMessage("Alias '${createdAlias.fullLink}' has been successfully created.")
            "redirect:/ui/alias"
        } catch (_: FullLinkExistsException) {
            bindResult.rejectValue("link", "", "alias is taken")
            model.addFormAttributes(aliasUiValue)
            "alias/new"
        } catch (e: Exception) {
            logger.error(e) { "Error occurred while creating a new alias: $aliasUiValue" }
            model.addFormAttributes(aliasUiValue)
            model.addErrorMessage(e)
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
        model.addFormAttributes(AliasUiValue(alias))
        return "alias/edit"
    }

    @PutMapping("/{aliasId}/edit")
    fun saveAlias(
        @PathVariable aliasId: Long,
        @Valid @ModelAttribute("alias") aliasUiValue: AliasUiValue,
        bindResult: BindingResult,
        model: Model,
        attributes: RedirectAttributes
    ): String {
        val namespace = namespaceService.findById(aliasUiValue.namespaceId)
        if (bindResult.hasErrors() || namespace == null) {
            if (namespace == null) {
                bindResult.rejectValue("namespaceId", "", "namespace not found")
            }
            model.addFormAttributes(aliasUiValue)
            logger.warn { "User input $aliasUiValue has failed validation ${bindResult.allErrors}" }
            return "alias/edit"
        }
        return try {
            val alias = aliasUiValueConverter.convertValueToModel(aliasUiValue, namespace).copy(id = aliasId)
            val updatedAlias = aliasService.update(alias)
            attributes.addSuccessMessage("Alias '${updatedAlias.fullLink}' has been successfully updated.")
            "redirect:/ui/alias"
        } catch (_: FullLinkExistsException) {
            bindResult.rejectValue("link", "", "alias is taken")
            model.addFormAttributes(aliasUiValue)
            "alias/edit"
        } catch (e: Exception) {
            logger.error(e) { "Error occurred while updating an existing alias: $aliasUiValue" }
            model.addFormAttributes(aliasUiValue)
            model.addErrorMessage(e)
            "alias/edit"
        }
    }

    @DeleteMapping("/{aliasId}")
    fun deleteAlias(
        @PathVariable aliasId: Long,
        attributes: RedirectAttributes
    ): String {
        return try {
            aliasService.deleteById(aliasId)
            attributes.addSuccessMessage("Alias #$aliasId has been successfully deleted.")
            "redirect:/ui/alias"
        } catch (e: Exception) {
            logger.error(e) { "Error occurred while deleting an alias: #$aliasId" }
            attributes.addErrorMessage(e)
            "redirect:/ui/alias"
        }
    }

    private fun Model.addFormAttributes(alias: AliasUiValue): Model = this.apply {
        addAttribute("alias", alias)
        addAttribute("namespaces", namespaceService.findAll())
    }

    companion object : KLogging()
}