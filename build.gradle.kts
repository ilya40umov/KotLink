import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

allprojects {
    group = "org.kotlink"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
        jcenter()
        maven("https://dl.bintray.com/kotlin/exposed")
    }
}

buildscript {
    val kotlinVersion by extra { "1.2.41" }
    val springBootVersion by extra { "2.0.2.RELEASE" }
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.springframework.boot:spring-boot-gradle-plugin:$springBootVersion")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath("org.jetbrains.kotlin:kotlin-allopen:$kotlinVersion")
    }
}

plugins {
    java
    jacoco
}

apply {
    plugin("kotlin")
    plugin("kotlin-spring")
    plugin("org.springframework.boot")
    plugin("io.spring.dependency-management")
}

configure<JavaPluginConvention> {
    setSourceCompatibility(1.8)
    setTargetCompatibility(1.8)
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
            freeCompilerArgs = listOf("-Xjsr305=strict")
        }
    }
    tasks.withType<Test> {
        testLogging.apply {
            events("passed", "skipped", "failed")
            showStandardStreams = true
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
}

dependencies {
    val kotlinLoggingVersion by extra { "1.4.9" }
    val postgresDriverVersion by extra { "42.2.2" }
    val flywayVersion by extra { "5.0.7" }
    val exposedVersion by extra { "0.10.2" }
    val thymeleafLayoutDialectVersion by extra { "2.3.0" }

    compile("org.springframework.boot:spring-boot-starter-actuator")
    compile("org.springframework.boot:spring-boot-starter-aop")
    compile("org.springframework.boot:spring-boot-starter-cache")
    compile("org.springframework.boot:spring-boot-starter-validation")
    compile("org.springframework.boot:spring-boot-starter-web")
    compile("org.springframework.boot:spring-boot-starter-webflux")
    compile("org.springframework.boot:spring-boot-starter-thymeleaf")
    compile("org.springframework.boot:spring-boot-starter-jdbc")

    compile("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    compile("org.jetbrains.kotlin:kotlin-reflect")

    compile("com.fasterxml.jackson.module:jackson-module-kotlin")
    compile("io.github.microutils:kotlin-logging:$kotlinLoggingVersion")
    compile("org.postgresql:postgresql:$postgresDriverVersion")
    compile("org.flywaydb:flyway-core:$flywayVersion")
    compile("org.jetbrains.exposed:exposed:$exposedVersion")
    compile("org.jetbrains.exposed:spring-transaction:$exposedVersion")
    compile("nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect:$thymeleafLayoutDialectVersion")

    val kluentVersion by extra { "1.36" }
    val kJsonAssertVersion by extra { "0.1.0" }
    val mockitoKotlinVersion by extra { "2.0.0-alpha01" }

    testCompile("org.springframework.boot:spring-boot-starter-test")
    testCompile("io.projectreactor:reactor-test")
    testCompile("org.amshove.kluent:kluent:$kluentVersion")
    testCompile("com.gregwoodfill.assert:kotlin-json-assert:$kJsonAssertVersion")
    testCompile("com.nhaarman.mockitokotlin2:mockito-kotlin:$mockitoKotlinVersion")
}
