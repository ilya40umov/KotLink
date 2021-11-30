package org.kotlink

import mu.KLogging
import org.kotlink.core.account.UserAccountService
import org.kotlink.core.namespace.NamespaceRepo
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * Responsible for changing the owner of the default namespace to the current admin,
 * if a such has been configured.
 */
@Component
@ConditionalOnProperty("kotlink.security.admin-email")
class DefaultNamespaceAppropriator(
    @Value("\${kotlink.security.admin-email}") private val adminEmail: String,
    private val namespaceRepo: NamespaceRepo,
    private val userAccountService: UserAccountService
) : ApplicationListener<ContextRefreshedEvent> {

    @Transactional
    override fun onApplicationEvent(event: ContextRefreshedEvent) {
        val defaultNamespace = namespaceRepo.findByKeyword("")
            ?: throw IllegalStateException("Default namespace can't be found in DB")
        if (defaultNamespace.ownerAccount.email != adminEmail) {
            logger.warn { "Changing the owner of the default namespace to the current admin: $adminEmail" }
            val adminUserAccount = userAccountService.findOrCreateAccountForEmail(adminEmail)
            namespaceRepo.update(defaultNamespace.copy(ownerAccount = adminUserAccount))
        }
    }

    companion object : KLogging()
}