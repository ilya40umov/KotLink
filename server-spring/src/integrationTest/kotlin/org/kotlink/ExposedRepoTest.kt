package org.kotlink

import org.kotlink.config.ExposedConfig
import org.kotlink.core.exposed.DatabaseExceptionAspect
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration
import org.springframework.boot.test.autoconfigure.filter.AnnotationCustomizableTypeExcludeFilter
import org.springframework.boot.test.autoconfigure.filter.TypeExcludeFilters
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureJdbc
import org.springframework.boot.test.context.SpringBootTestContextBootstrapper
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.context.annotation.Import
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.stereotype.Repository
import org.springframework.test.context.BootstrapWith
import org.springframework.transaction.annotation.Transactional
import java.lang.annotation.Inherited

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
@BootstrapWith(SpringBootTestContextBootstrapper::class)
@OverrideAutoConfiguration(enabled = false)
@TypeExcludeFilters(ExposedRepoTypeExcludeFilter::class)
@ImportAutoConfiguration
@Transactional
@AutoConfigureJdbc
@EnableAspectJAutoProxy
@Import(DatabaseExceptionAspect::class, ExposedConfig::class)
annotation class ExposedRepoTest

internal class ExposedRepoTypeExcludeFilter(testClass: Class<*>) : AnnotationCustomizableTypeExcludeFilter() {

    private val defaultIncludes = setOf(Repository::class.java)

    private val annotation: ExposedRepoTest? =
        AnnotatedElementUtils.getMergedAnnotation(testClass, ExposedRepoTest::class.java)

    override fun hasAnnotation(): Boolean = this.annotation != null

    override fun getFilters(type: FilterType): Array<ComponentScan.Filter> = emptyArray()

    override fun isUseDefaultFilters(): Boolean = true

    override fun getDefaultIncludes(): Set<Class<*>> = defaultIncludes

    override fun getComponentIncludes(): Set<Class<*>> = emptySet()
}
