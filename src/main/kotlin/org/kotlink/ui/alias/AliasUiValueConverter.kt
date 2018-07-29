package org.kotlink.ui.alias

import org.kotlink.core.account.UserAccountService
import org.kotlink.core.alias.Alias
import org.kotlink.core.exposed.RecordNotFoundException
import org.kotlink.core.namespace.Namespace
import org.springframework.stereotype.Component

@Component
class AliasUiValueConverter(
    private val userAccountService: UserAccountService
) {

    fun convertValueToModel(aliasUiValue: AliasUiValue, namespace: Namespace): Alias =
        aliasUiValue.run {
            Alias(
                id = id,
                namespace = namespace,
                link = link,
                redirectUrl = redirectUrl,
                description = description,
                ownerAccount = userAccountService.findByUserEmail(ownerAccountEmail)
                    ?: throw RecordNotFoundException("Couldn't find account for: ${aliasUiValue.ownerAccountEmail}")
            )
        }
}