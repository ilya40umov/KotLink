package org.kotlink.ui.namespace

import arrow.core.Either
import org.apache.commons.validator.routines.EmailValidator
import org.kotlink.domain.account.UserAccountService
import org.kotlink.domain.namespace.Namespace
import org.kotlink.domain.namespace.NamespaceService
import org.kotlink.framework.ui.FormErrors
import org.kotlink.framework.ui.FormErrorsCollector

class NamespaceFormConverter(
    private val namespaceService: NamespaceService,
    private val userAccountService: UserAccountService
) {
    fun getPotentialErrors(
        formData: NamespaceForm
    ): FormErrors = FormErrorsCollector(
        treatAllErrorsAsPotential = true
    ).also { errors ->
        formData.convertLinkPrefix(errors, isEdit = false)
        formData.convertDescription(errors)
        formData.convertOwnerEmail(errors)
    }

    fun convertToNamespace(
        formData: NamespaceForm,
        isEdit: Boolean
    ): Either<FormErrors, Namespace> {
        val errors = FormErrorsCollector()
        val linkPrefix = formData.convertLinkPrefix(errors, isEdit)
        val description = formData.convertDescription(errors)
        val ownerEmail = formData.convertOwnerEmail(errors)
        return if (errors.allFieldValid()) {
            Either.Right(
                Namespace(
                    linkPrefix = linkPrefix,
                    description = description,
                    ownerEmail = ownerEmail
                )
            )
        } else {
            Either.Left(errors)
        }
    }

    private fun NamespaceForm.convertLinkPrefix(
        errors: FormErrorsCollector,
        isEdit: Boolean
    ): String {
        val linkPrefix = linkPrefix?.trim() ?: ""
        if (isEdit && linkPrefix.isEmpty()) {
            return linkPrefix // default namespace has no prefix, but we still allow to edit it
        }
        val isValid =
            linkPrefix.isNotBlank()
                && linkPrefix.length <= Namespace.MAX_LINK_PREFIX_LENGTH
                && linkPrefix.matches(Namespace.LINK_PREFIX_REGEX)
        errors.addForField(
            field = NamespaceForm::linkPrefix.name,
            error = "Required. Must contain up to ${Namespace.MAX_LINK_PREFIX_LENGTH} alphanumerical, " +
                "lowercase characters and/or whitespaces.",
            actualError = !isValid
        )
        if (!isEdit && isValid && namespaceService.findByLinkPrefix(linkPrefix) != null) {
            errors.addForField(
                field = NamespaceForm::linkPrefix.name,
                error = "Such a namespace already exists."
            )
        }
        return linkPrefix
    }

    private fun NamespaceForm.convertDescription(errors: FormErrorsCollector): String {
        val description = description?.trim() ?: ""
        val isValid = description.isNotBlank() && description.length <= Namespace.MAX_DESCRIPTION_LENGTH
        errors.addForField(
            field = NamespaceForm::description.name,
            error = "Required. Must contain up to ${Namespace.MAX_DESCRIPTION_LENGTH} characters.",
            actualError = !isValid
        )
        return description
    }

    private fun NamespaceForm.convertOwnerEmail(errors: FormErrorsCollector): String {
        val ownerEmail = ownerEmail?.trim() ?: ""
        var isValid = ownerEmail.isNotBlank() && ownerEmail.length <= Namespace.MAX_EMAIL_LENGTH
        errors.addForField(
            field = NamespaceForm::ownerEmail.name,
            error = "Required. Must contain up to ${Namespace.MAX_EMAIL_LENGTH} characters.",
            actualError = !isValid
        )
        if (isValid && !EmailValidator.getInstance().isValid(ownerEmail)) {
            errors.addForField(
                field = NamespaceForm::ownerEmail.name,
                error = "Must be a valid email."
            )
            isValid = false
        }
        if (isValid && userAccountService.findByUserEmail(ownerEmail) == null) {
            errors.addForField(
                field = NamespaceForm::ownerEmail.name,
                error = "User with this email does not exist."
            )
        }
        return ownerEmail
    }
}