package org.kotlink.ui.alias

import arrow.core.Either
import org.apache.commons.validator.routines.EmailValidator
import org.apache.commons.validator.routines.UrlValidator
import org.kotlink.domain.account.UserAccountService
import org.kotlink.domain.alias.Alias
import org.kotlink.domain.alias.AliasService
import org.kotlink.domain.namespace.NamespaceService
import org.kotlink.framework.ui.FormErrors
import org.kotlink.framework.ui.FormErrorsCollector

class AliasFormConverter(
    private val aliasService: AliasService,
    private val namespaceService: NamespaceService,
    private val userAccountService: UserAccountService
) {
    fun getPotentialErrors(
        formData: AliasForm
    ): FormErrors = FormErrorsCollector(
        treatAllErrorsAsPotential = true
    ).also { errors ->
        val linkPrefix = formData.convertLinkPrefix(errors)
        formData.convertLink(errors, linkPrefix, isEdit = false)
        formData.convertRedirectUrl(errors)
        formData.convertDescription(errors)
        formData.convertOwnerEmail(errors)
    }

    fun convertToAlias(
        formData: AliasForm,
        isEdit: Boolean
    ): Either<FormErrors, Alias> {
        val errors = FormErrorsCollector()
        val linkPrefix = formData.convertLinkPrefix(errors)
        val link = formData.convertLink(errors, linkPrefix, isEdit)
        val redirectUrl = formData.convertRedirectUrl(errors)
        val description = formData.convertDescription(errors)
        val ownerEmail = formData.convertOwnerEmail(errors)
        return if (errors.allFieldValid()) {
            Either.Right(
                Alias(
                    linkPrefix = linkPrefix,
                    link = link,
                    redirectUrl = redirectUrl,
                    description = description,
                    ownerEmail = ownerEmail
                )
            )
        } else {
            Either.Left(errors)
        }
    }

    private fun AliasForm.convertLinkPrefix(errors: FormErrorsCollector): String {
        val linkPrefix = linkPrefix?.trim() ?: ""
        val isValid = namespaceService.findByLinkPrefix(linkPrefix) != null
        if (!isValid) {
            errors.addForField(
                field = AliasForm::linkPrefix.name,
                error = "Namespace does not exist."
            )
        }
        return linkPrefix
    }

    private fun AliasForm.convertLink(
        errors: FormErrorsCollector,
        linkPrefix: String,
        isEdit: Boolean
    ): String {
        val link = link?.trim() ?: ""
        val fullLink = if (linkPrefix.isNotBlank()) "$linkPrefix $link" else link
        val isValid = link.isNotBlank() && link.length <= Alias.MAX_LINK_LENGTH && link.matches(Alias.LINK_REGEX)
        errors.addForField(
            field = AliasForm::link.name,
            error = "Required. Must contain up to ${Alias.MAX_LINK_LENGTH} alphanumerical, " +
                "lowercase characters and/or whitespaces.",
            actualError = !isValid
        )
        if (!isEdit && isValid && aliasService.findByFullLink(fullLink) != null) {
            errors.addForField(
                field = AliasForm::link.name,
                error = "Such a link already exists."
            )
        }
        return link
    }

    private fun AliasForm.convertRedirectUrl(errors: FormErrorsCollector): String {
        val redirectUrl = redirectUrl?.trim() ?: ""
        val isValid = redirectUrl.isNotBlank() && redirectUrl.length <= Alias.MAX_REDIRECT_URL_LENGTH
        errors.addForField(
            field = AliasForm::redirectUrl.name,
            error = "Required. Must contain up to ${Alias.MAX_REDIRECT_URL_LENGTH} characters.",
            actualError = !isValid
        )
        if (isValid && !UrlValidator.getInstance().isValid(redirectUrl)) {
            errors.addForField(
                field = AliasForm::redirectUrl.name,
                error = "Must be a valid URL."
            )
        }
        return redirectUrl
    }

    private fun AliasForm.convertDescription(errors: FormErrorsCollector): String {
        val description = description?.trim() ?: ""
        val isValid = description.isNotBlank() && description.length <= Alias.MAX_DESCRIPTION_LENGTH
        errors.addForField(
            field = AliasForm::description.name,
            error = "Required. Must contain up to ${Alias.MAX_DESCRIPTION_LENGTH} characters.",
            actualError = !isValid
        )
        return description
    }

    private fun AliasForm.convertOwnerEmail(errors: FormErrorsCollector): String {
        val ownerEmail = ownerEmail?.trim() ?: ""
        var isValid = ownerEmail.isNotBlank() && ownerEmail.length <= Alias.MAX_EMAIL_LENGTH
        errors.addForField(
            field = AliasForm::ownerEmail.name,
            error = "Required. Must contain up to ${Alias.MAX_EMAIL_LENGTH} characters.",
            actualError = !isValid
        )
        if (isValid && !EmailValidator.getInstance().isValid(ownerEmail)) {
            errors.addForField(
                field = AliasForm::ownerEmail.name,
                error = "Must be a valid email."
            )
            isValid = false
        }
        if (isValid && userAccountService.findByUserEmail(ownerEmail) == null) {
            errors.addForField(
                field = AliasForm::ownerEmail.name,
                error = "User with this email does not exist."
            )
        }
        return ownerEmail
    }
}