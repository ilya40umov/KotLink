package org.kotlink

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.kotlink.core.account.UserAccountService
import org.kotlink.core.namespace.NamespaceRepo
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.context.event.ContextRefreshedEvent

@ExtendWith(MockitoExtension::class)
class DefaultNamespaceAppropriatorTest(
    @Mock private val namespaceRepo: NamespaceRepo,
    @Mock private val userAccountService: UserAccountService,
    @Mock private val event: ContextRefreshedEvent
) {

    @Test
    fun `'onApplicationEvent' should change owner of the default namespace if it's not current admin`() {
        whenever(namespaceRepo.findByKeyword(""))
            .thenReturn(DEFAULT_NAMESPACE)
        whenever(userAccountService.findOrCreateAccountForEmail("admin12345@kotlink.com"))
            .thenReturn(TEST_ACCOUNT.copy(email = "admin12345@kotlink.com"))

        appropriatorWithAdminEmail("admin12345@kotlink.com").onApplicationEvent(event)

        verify(namespaceRepo).update(any())
    }

    @Test
    fun `'onApplicationEvent' should not change owner of the default namespace if it's current admin`() {
        whenever(namespaceRepo.findByKeyword(""))
            .thenReturn(DEFAULT_NAMESPACE)

        appropriatorWithAdminEmail(DEFAULT_NAMESPACE.ownerAccount.email).onApplicationEvent(event)

        verify(namespaceRepo, never()).update(any())
    }

    private fun appropriatorWithAdminEmail(adminEmail: String) =
        DefaultNamespaceAppropriator(adminEmail, namespaceRepo, userAccountService)
}