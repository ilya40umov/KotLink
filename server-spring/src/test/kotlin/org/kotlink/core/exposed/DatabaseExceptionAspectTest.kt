package org.kotlink.core.exposed

import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.whenever
import org.amshove.kluent.shouldThrow
import org.aspectj.lang.ProceedingJoinPoint
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import java.lang.reflect.UndeclaredThrowableException

@ExtendWith(MockitoExtension::class)
class DatabaseExceptionAspectTest {

    private val aspect = DatabaseExceptionAspect()

    @Test
    fun `'normalizeExceptions' should unwrap UndeclaredThrowableException as DatabaseException`(
        @Mock joinPoint: ProceedingJoinPoint
    ) {
        doThrow(UndeclaredThrowableException(Exception("fake error")))
            .whenever(joinPoint)
            .proceed();

        {
            aspect.normalizeExceptions(joinPoint)
        } shouldThrow DatabaseException::class
    }
}