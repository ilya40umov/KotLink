package org.kotlink.core.ipblock

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.kotlink.core.OperationDeniedException
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.stereotype.Component
import javax.servlet.http.HttpServletRequest

@Aspect
@Component
@ConditionalOnExpression("'\${kotlink.security.restrict-edits-to-ip-regex}' != '.*'")
class EditOpIpBasedRestrictingAspect(
    @Value("\${kotlink.security.restrict-edits-to-ip-regex}") restrictEditsToIpMask: String,
    private val request: HttpServletRequest
) {

    private val restrictEditsToIpRegex = restrictEditsToIpMask.toRegex()

    @Around("execution(* org.kotlink.core..*(..)) && @annotation(org.kotlink.core.ipblock.EditOp)")
    @Throws(Throwable::class)
    fun checkIp(joinPoint: ProceedingJoinPoint): Any? {
        if (!restrictEditsToIpRegex.matches(request.remoteAddr)) {
            throw OperationDeniedException("This operation is not permitted from your IP address")
        }
        return joinPoint.proceed()
    }
}