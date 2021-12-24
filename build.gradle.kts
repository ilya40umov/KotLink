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

    id("io.gitlab.arturbosch.detekt") version "1.19.0"
    id("org.jlleitschuh.gradle.ktlint") version "10.2.0"
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

    dependencies {
        "implementation"(platform("org.jetbrains.kotlin:kotlin-bom"))
        "implementation"(platform("org.apache.logging.log4j:log4j-bom:2.15.0"))
        "implementation"("org.slf4j:slf4j-api:1.7.32")
        "implementation"("org.apache.logging.log4j:log4j-core")
        "implementation"("org.apache.logging.log4j:log4j-jul")
        "implementation"("io.github.microutils:kotlin-logging:2.1.16")

        "runtimeOnly"("org.apache.logging.log4j:log4j-slf4j-impl")

        val jupiterVersion = "5.8.2"
        "testImplementation"("org.junit.jupiter:junit-jupiter-api:$jupiterVersion")
        "testRuntimeOnly"("org.junit.jupiter:junit-jupiter-engine:$jupiterVersion")

        "testImplementation"("org.amshove.kluent:kluent:1.68")
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