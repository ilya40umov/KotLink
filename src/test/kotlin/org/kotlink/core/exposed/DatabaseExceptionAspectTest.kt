package org.kotlink.core.exposed

import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.amshove.kluent.shouldThrow
import org.aspectj.lang.ProceedingJoinPoint
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import java.lang.reflect.UndeclaredThrowableException

@RunWith(MockitoJUnitRunner::class)
class DatabaseExceptionAspectTest {

    private val joinPoint = mock<ProceedingJoinPoint> {}
    private val aspect = DatabaseExceptionAspect()

    @Test
    fun `'normalizeExceptions' should unwrap UndeclaredThrowableException as DatabaseException`() {
        doThrow(UndeclaredThrowableException(Exception("fake error")))
            .whenever(joinPoint)
            .proceed();

        {
            aspect.normalizeExceptions(joinPoint)
        } shouldThrow DatabaseException::class
    }
}