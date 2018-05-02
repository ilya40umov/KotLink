package org.kotlink

import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureJdbc
import org.springframework.boot.test.context.SpringBootTestContextBootstrapper
import org.springframework.test.context.BootstrapWith
import org.springframework.transaction.annotation.Transactional

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@BootstrapWith(SpringBootTestContextBootstrapper::class)
@OverrideAutoConfiguration(enabled = false)
@ImportAutoConfiguration
@Transactional
@AutoConfigureJdbc
annotation class ExposedRepoTest