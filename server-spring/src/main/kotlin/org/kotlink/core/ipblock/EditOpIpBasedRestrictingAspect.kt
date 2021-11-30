package org.kotlink.core.ipblock

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.kotlink.core.OperationDeniedException
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.web.util.matcher.IpAddressMatcher
import org.springframework.stereotype.Component
import javax.servlet.http.HttpServletRequest

@Aspect
@Component
class EditOpIpBasedRestrictingAspect(
    @Value("\${kotlink.security.restrict-edits-to-ip-regex}") restrictEditsToIpMask: String,
    @Value("\${kotlink.security.restrict-edits-to-ip-cidrs}") restrictEditsToIpCidrs: String,
    private val request: HttpServletRequest
) {

    private val restrictEditsToIpRegex = restrictEditsToIpMask.toRegex()
    private val ipMatchers = restrictEditsToIpCidrs.split(",").map { IpAddressMatcher(it) }

    @Around("execution(* org.kotlink.core..*(..)) && @annotation(org.kotlink.core.ipblock.EditOp)")
    @Throws(Throwable::class)
    fun checkIp(joinPoint: ProceedingJoinPoint): Any? {
        if (!restrictEditsToIpRegex.matches(request.remoteAddr)) {
            throw OperationDeniedException("This operation is not permitted from your IP address")
        }
        if (ipMatchers.none { it.matches(request) }) {
            throw OperationDeniedException("This operation is not permitted from your IP address")
        }
        return joinPoint.proceed()
    }
}