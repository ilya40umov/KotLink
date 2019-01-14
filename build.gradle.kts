import io.gitlab.arturbosch.detekt.Detekt
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "org.kotlink"

repositories {
    jcenter()
}

plugins {
    // built-in plugins
    java
    jacoco
    // versions of all kotlin plugins are resolved by logic in 'settings.gradle.kts'
    kotlin("jvm")
    kotlin("kapt")
    kotlin("plugin.spring")
    kotlin("plugin.allopen")
    // version of spring boot plugin is also resolved by 'settings.gradle.kts'
    id("org.springframework.boot")
    // other plugins require a version to be mentioned
    id("io.spring.dependency-management") version "1.0.6.RELEASE"
    id("org.owasp.dependencycheck") version "4.0.1"
    id("io.gitlab.arturbosch.detekt") version "1.0.0-RC12"
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
            freeCompilerArgs = listOf("-Xjsr305=strict")
        }
    }
    withType<Test> {
        useJUnitPlatform {
            systemProperty("spring.datasource.url", "jdbc:postgresql://localhost:45432/kotlink")
            systemProperty("spring.redis.url", "redis://localhost:46379")
        }
        testLogging.apply {
            events("passed", "skipped", "failed")
            exceptionFormat = TestExceptionFormat.FULL
        }
    }
    withType<JacocoReport> {
        reports {
            xml.apply {
                isEnabled = true
            }
            html.apply {
                isEnabled = false
            }
        }
    }
    withType<Detekt> {
        description = "Runs Detekt code analysis"
        config = files("src/main/resources/default-detekt-config.yml")
    }
}

dependencies {

    val springBootVersion: String by project.extra
    val exposedVersion = "0.11.2"
    val logbackVersion = "1.2.3"

    compile("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    compile("org.jetbrains.kotlin:kotlin-reflect")

    compile("org.springframework.boot:spring-boot-starter-actuator")
    compile("org.springframework.boot:spring-boot-starter-aop")
    compile("org.springframework.boot:spring-boot-starter-cache")
    compile("org.springframework.boot:spring-boot-starter-validation")
    compile("org.springframework.boot:spring-boot-starter-web")
    compile("org.springframework.boot:spring-boot-starter-webflux")
    compile("org.springframework.boot:spring-boot-starter-thymeleaf")
    compile("org.springframework.boot:spring-boot-starter-jdbc")
    compile("org.springframework.boot:spring-boot-starter-data-redis")
    compile("org.springframework.boot:spring-boot-starter-security")
    compile("org.springframework.session:spring-session-jdbc")
    compile("org.springframework.session:spring-session-data-redis")
    compile("org.springframework.boot:spring-boot-devtools")

    compileOnly("org.springframework.boot:spring-boot-configuration-processor")
    kapt("org.springframework.boot:spring-boot-configuration-processor")

    compile("org.springframework.security.oauth.boot:spring-security-oauth2-autoconfigure:$springBootVersion")
    compile("org.thymeleaf.extras:thymeleaf-extras-springsecurity5")

    compile("com.github.ben-manes.caffeine:caffeine")
    compile("io.micrometer:micrometer-registry-prometheus")
    compile("com.fasterxml.jackson.module:jackson-module-kotlin")
    compile("io.github.microutils:kotlin-logging:1.6.22")
    compile("org.slf4j:slf4j-api:1.7.25")
    compile("ch.qos.logback:logback-classic:$logbackVersion")
    compile("ch.qos.logback:logback-core:$logbackVersion")
    compile("ch.qos.logback:logback-access:$logbackVersion")
    compile("org.apache.logging.log4j:log4j-to-slf4j:2.11.1")
    compile("org.postgresql:postgresql:42.2.5")
    compile("org.flywaydb:flyway-core:5.2.4")
    compile("org.jetbrains.exposed:exposed:$exposedVersion")
    compile("org.jetbrains.exposed:spring-transaction:$exposedVersion")
    compile("nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect:2.3.0")

    testCompile("org.springframework.boot:spring-boot-starter-test")
    testCompile("org.junit.jupiter:junit-jupiter-api:5.3.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.3.2")
    testCompile("org.amshove.kluent:kluent:1.44")
    testCompile("io.projectreactor:reactor-test")
    testCompile("com.nhaarman.mockitokotlin2:mockito-kotlin:2.0.0")
    testCompile("org.mockito:mockito-core:2.23.4") {
        isForce = true
        because("version that is enforced by Spring Boot is not compatible with Java 11")
    }
    testCompile("net.bytebuddy:byte-buddy:1.9.3") {
        isForce = true
        because("version that is enforced by Spring Boot is not compatible with Java 11")
    }
    testCompile("org.mockito:mockito-junit-jupiter:2.23.4")
}
