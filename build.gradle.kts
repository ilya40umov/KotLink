import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import io.gitlab.arturbosch.detekt.Detekt
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    jacoco

    val kotlinVersion = "1.6.0"
    kotlin("jvm") version kotlinVersion apply false
    kotlin("kapt") version kotlinVersion apply false
    kotlin("plugin.spring") version kotlinVersion apply false
    kotlin("plugin.allopen") version kotlinVersion apply false

    val springVersion = "2.6.1"
    id("org.springframework.boot") version springVersion apply false

    id("io.spring.dependency-management") version "1.0.11.RELEASE" apply false
    id("com.avast.gradle.docker-compose") version "0.14.11" apply false
    id("io.gitlab.arturbosch.detekt") version "1.19.0"
    id("org.jlleitschuh.gradle.ktlint") version "10.1.0"
    id("com.github.ben-manes.versions") version "0.39.0"
}

allprojects {
    group = "org.kotlink"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenLocal()
        mavenCentral()
    }
}

subprojects {
    apply<JavaPlugin>()
    apply<JacocoPlugin>()

    configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    tasks {
        withType<KotlinCompile> {
            kotlinOptions {
                jvmTarget = "11"
                freeCompilerArgs = listOf("-Xjsr305=strict")
            }
        }
        withType<Test> {
            testLogging.apply {
                events("passed", "skipped", "failed")
                exceptionFormat = TestExceptionFormat.FULL
                debug {
                    showStandardStreams = true
                }
            }
        }
    }
}

dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.19.0")
}

ktlint {
    enableExperimentalRules.set(false)
    disabledRules.set(setOf("final-newline"))
    filter {
        disabledRules.set(setOf("final-newline", "indent"))
    }
    filter {
        exclude("**.kts")
    }
}

tasks {
    val detekt by named("detekt", Detekt::class) {
        description = "Runs Detekt to perform code analysis"
        buildUponDefaultConfig = true
        config.setFrom(files(projectDir.resolve("detekt.yml")))
        setSource(files(*subprojects.map { "${it.projectDir}/src" }.toTypedArray()))
        autoCorrect = false
        ignoreFailures = false
        reports {
            html.required.set(false)
            xml.required.set(false)
            txt.required.set(false)
            sarif.required.set(true)
        }
    }
    val check by registering {
        dependsOn(detekt)
    }
    register("build") {
        dependsOn(check)
    }
    register("jacocoAggregatedReport", JacocoReport::class) {
        reports.html.required.set(true)
        reports.xml.required.set(true)
        reports.csv.required.set(false)
        subprojects.filter { it.name != "assembly" }.forEach { subProject ->
            dependsOn(subProject.tasks.findByPath("test"))
            executionData(subProject.tasks.findByPath("test"))
            additionalSourceDirs(files("${subProject.projectDir}/src/kotlin/main"))
            additionalClassDirs(files("${subProject.buildDir}/classes/kotlin/main"))
        }
    }

    fun isNonStable(version: String): Boolean {
        val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.toUpperCase().contains(it) }
        val regex = "^[0-9,.v-]+(-r)?$".toRegex()
        val isStable = stableKeyword || regex.matches(version)
        return isStable.not()
    }

    named<DependencyUpdatesTask>("dependencyUpdates") {
        checkConstraints = true
        revision = "release"
        gradleReleaseChannel = "current"
        rejectVersionIf {
            isNonStable(candidate.version) && !isNonStable(currentVersion)
        }
    }
}