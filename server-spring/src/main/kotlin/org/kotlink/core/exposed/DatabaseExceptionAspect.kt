package org.kotlink.core.exposed

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.stereotype.Component
import java.lang.reflect.UndeclaredThrowableException

/**
 * Makes sure PSQLException, ExposedSQLException, UndeclaredThrowableException and such
 * aren't thrown by the repositories.
 */
@Aspect
@Component
@Suppress("TooGenericExceptionCaught", "ThrowsCount", "SwallowedException", "RethrowCaughtException")
class DatabaseExceptionAspect {

    @Around("execution(* org.kotlink.core..*(..)) && @target(org.springframework.stereotype.Repository)")
    @Throws(Throwable::class)
    fun normalizeExceptions(joinPoint: ProceedingJoinPoint): Any? {
        try {
            return joinPoint.proceed()
        } catch (e: UndeclaredThrowableException) {
            // unwrapping UndeclaredThrowableException and re-wrapping its cause into DatabaseException
            throw DatabaseException(e.cause?.message, e.cause)
        } catch (e: RuntimeException) {
            // unchecked exceptions are safe to re-throw as they aren't translated into UndeclaredThrowableException
            throw e
        } catch (e: Exception) {
            // we wrap any checked exception into DatabaseException, so that they don
            throw DatabaseException(e.message, e)
        }
    }
}