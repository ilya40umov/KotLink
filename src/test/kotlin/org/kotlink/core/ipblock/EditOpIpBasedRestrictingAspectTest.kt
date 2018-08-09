package org.kotlink.core.ipblock

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.amshove.kluent.shouldThrow
import org.aspectj.lang.ProceedingJoinPoint
import org.junit.Test
import org.junit.runner.RunWith
import org.kotlink.core.OperationDeniedException
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import javax.servlet.http.HttpServletRequest

@RunWith(MockitoJUnitRunner::class)
class EditOpIpBasedRestrictingAspectTest {

    @Mock
    private lateinit var request: HttpServletRequest

    @Mock
    private lateinit var joinPoint: ProceedingJoinPoint

    @Test
    fun `'checkIp' should throw exception if IP is not allowed to perform EditOp`() {
        doReturn("8.8.8.8").whenever(request).remoteAddr

        {
            EditOpIpBasedRestrictingAspect(
                restrictEditsToIpMask = "123\\.123\\.123\\.123",
                request = request
            ).checkIp(joinPoint)
        } shouldThrow OperationDeniedException::class
    }

    @Test
    fun `'checkIp' should let the method call proceed if IP is allowed to perform EditOp`() {
        doReturn("8.8.8.8").whenever(request).remoteAddr

        EditOpIpBasedRestrictingAspect(
            restrictEditsToIpMask = "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}",
            request = request
        ).checkIp(joinPoint)

        verify(joinPoint).proceed()
    }
}
