package org.kotlink.ui.namespace

import org.kotlink.core.account.UserAccountService
import org.kotlink.core.exposed.RecordNotFoundException
import org.kotlink.core.namespace.Namespace
import org.springframework.stereotype.Component

@Component
class NamespaceUiValueConverter(
    private val userAccountService: UserAccountService
) {

    fun convertValueToModel(namespaceUiValue: NamespaceUiValue): Namespace =
        namespaceUiValue.run {
            Namespace(
                id = id,
                keyword = keyword,
                description = description,
                ownerAccount = userAccountService.findByUserEmail(ownerAccountEmail)
                    ?: throw RecordNotFoundException("Couldn't find account for: ${namespaceUiValue.ownerAccountEmail}")
            )
        }
}