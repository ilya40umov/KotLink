package org.kotlink

import org.kotlink.ui.UiTestConfig
import org.springframework.context.annotation.Import
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import java.lang.annotation.Inherited

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
@Import(MockMvcExtras::class, UiTestConfig::class)
@ActiveProfiles("mvc-test")
@WithMockUser
annotation class WithMockMvcSetUp