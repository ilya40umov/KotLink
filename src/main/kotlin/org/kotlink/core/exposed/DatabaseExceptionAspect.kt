package org.kotlink.core.exposed

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.postgresql.util.PSQLException
import org.springframework.stereotype.Component
import java.lang.reflect.UndeclaredThrowableException

/** Makes sure PSQLException and UndeclaredThrowableException don't get thrown from the repositories. */
@Aspect
@Component
class DatabaseExceptionAspect {

    @Around("execution(* org.kotlink.core..*(..)) && @target(org.springframework.stereotype.Repository)")
    @Throws(Throwable::class)
    fun normalizeExceptions(joinPoint: ProceedingJoinPoint): Any? {
        try {
            return joinPoint.proceed()
        } catch (e: PSQLException) {
            throw DatabaseException(e.message, e)
        } catch (e: UndeclaredThrowableException) {
            throw DatabaseException(e.cause?.message, e.cause)
        }
    }
}