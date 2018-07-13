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
    val kotlinVersion by extra { "1.2.51" }
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
    setSourceCompatibility(JavaVersion.VERSION_1_8)
    setTargetCompatibility(JavaVersion.VERSION_1_8)
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
            freeCompilerArgs = listOf("-Xjsr305=strict")
        }
    }
    tasks.withType<Test> {
        doFirst {
            systemProperty("spring.datasource.url", "jdbc:postgresql://localhost:45432/kotlink")
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
}

dependencies {

    val exposedVersion by extra { "0.10.2" }

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
    compile("org.springframework.boot:spring-boot-starter-security")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    compile("org.springframework.security.oauth.boot:spring-security-oauth2-autoconfigure:2.0.1.RELEASE")
    compile("org.thymeleaf.extras:thymeleaf-extras-springsecurity4")

    compile("com.fasterxml.jackson.module:jackson-module-kotlin")
    compile("io.github.microutils:kotlin-logging:1.4.9")
    compile("org.postgresql:postgresql:42.2.2")
    compile("org.flywaydb:flyway-core:5.0.7")
    compile("org.jetbrains.exposed:exposed:$exposedVersion")
    compile("org.jetbrains.exposed:spring-transaction:$exposedVersion")
    compile("nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect:2.3.0")

    testCompile("org.springframework.boot:spring-boot-starter-test")
    testCompile("io.projectreactor:reactor-test")
    testCompile("org.amshove.kluent:kluent:1.36")
    testCompile("com.gregwoodfill.assert:kotlin-json-assert:0.1.0")
    testCompile("com.nhaarman.mockitokotlin2:mockito-kotlin:2.0.0-alpha01")
}
