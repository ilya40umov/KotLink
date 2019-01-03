package org.kotlink.core.ipblock

import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.amshove.kluent.shouldThrow
import org.aspectj.lang.ProceedingJoinPoint
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.kotlink.core.OperationDeniedException
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import javax.servlet.http.HttpServletRequest

@ExtendWith(MockitoExtension::class)
class EditOpIpBasedRestrictingAspectTest(
    @Mock private val request: HttpServletRequest,
    @Mock private val joinPoint: ProceedingJoinPoint
) {

    init {
        whenever(request.remoteAddr).thenReturn("8.8.8.8")
    }

    @Test
    fun `'checkIp' should throw exception if IP is not allowed to perform EditOp`() {
        {
            EditOpIpBasedRestrictingAspect(
                restrictEditsToIpMask = "123\\.123\\.123\\.123",
                request = request
            ).checkIp(joinPoint)
        } shouldThrow OperationDeniedException::class
    }

    @Test
    fun `'checkIp' should let the method call proceed if IP is allowed to perform EditOp`() {
        EditOpIpBasedRestrictingAspect(
            restrictEditsToIpMask = "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}",
            request = request
        ).checkIp(joinPoint)

        verify(joinPoint).proceed()
    }
}
