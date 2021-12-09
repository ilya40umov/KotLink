import com.avast.gradle.dockercompose.ComposeSettings
import org.jetbrains.kotlin.gradle.utils.addExtendsFromRelation
import org.springframework.boot.gradle.tasks.bundling.BootJar
import org.springframework.boot.gradle.tasks.run.BootRun

plugins {
    `jvm-test-suite`

    kotlin("jvm")
    kotlin("kapt")
    kotlin("plugin.spring")
    kotlin("plugin.allopen")

    id("org.springframework.boot") version "2.6.1"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("com.avast.gradle.docker-compose") version "0.14.11"
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.springframework.boot:spring-boot-starter-log4j2")
    implementation("org.springframework.boot:spring-boot-devtools")

    implementation("org.springframework.session:spring-session-jdbc")
    implementation("org.springframework.session:spring-session-data-redis")

    compileOnly("org.springframework.boot:spring-boot-configuration-processor")
    kapt("org.springframework.boot:spring-boot-configuration-processor")

    implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity5")
    implementation("nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect:3.0.0")

    implementation("com.github.ben-manes.caffeine:caffeine")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    implementation("org.postgresql:postgresql:42.3.1")
    implementation("org.flywaydb:flyway-core:8.2.1")

    implementation(platform("org.jetbrains.exposed:exposed-bom:0.36.2"))
    implementation("org.jetbrains.exposed:exposed-core")
    implementation("org.jetbrains.exposed:exposed-jdbc")
    implementation("org.jetbrains.exposed:spring-transaction")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")

    testImplementation("io.projectreactor:reactor-test")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
    testImplementation("org.mockito:mockito-junit-jupiter:4.1.0")
}

configurations {
    all {
        // Exclude the default logging implementations used by Spring Boot.
        exclude("org.springframework.boot", "spring-boot-starter-logging")
    }
}

dockerCompose {
    projectName = "kotlink"
    projectNamePrefix = "kotlink_"
    useComposeFiles.set(listOf("docker-compose.yaml", "docker-compose-local.yaml"))
    // when set to "false", the plugin automatically tries to reconnect to the containers from the previous run
    stopContainers.set(false)

    createNested("integrationTest").apply {
        projectName = "kotlink-ci"
        projectNamePrefix = "kotlink_ci_"
        useComposeFiles.set(listOf("docker-compose.yaml", "docker-compose-ci.yaml"))
    }
}

fun ComposeSettings.postgresUrl() =
    "jdbc:postgresql://localhost:${servicesInfos["postgresql"]?.port ?: 5432}/kotlink"

fun ComposeSettings.redisUrl() =
    "redis://localhost:${servicesInfos["redis"]?.port ?: 6379}"

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
        }
        register("integrationTest", JvmTestSuite::class) {
            useJUnitJupiter()
            addExtendsFromRelation(
                extendingConfigurationName = "integrationTestImplementation",
                extendsFromConfigurationName = "testImplementation",
                forced = true
            )
            dependencies {
                implementation(project)
            }
            targets {
                all {
                    testTask.configure {
                        shouldRunAfter(test)
                        dependsOn("integrationTestComposeUp")
                        doFirst {
                            systemProperty("spring.datasource.url", dockerCompose.nested("integrationTest").postgresUrl())
                            systemProperty("spring.redis.url", dockerCompose.nested("integrationTest").redisUrl())
                        }
                    }
                }
            }
        }
    }
}

tasks {
    named("check") {
        dependsOn(testing.suites.named("integrationTest"))
    }
    withType<BootRun> {
        dependsOn("composeUp")
        doFirst {
            systemProperty("spring.datasource.url", dockerCompose.postgresUrl())
            systemProperty("spring.redis.url", dockerCompose.redisUrl())
        }
    }
    withType<BootJar> {
        archiveFileName.set("kotlink.jar")
    }
    register("psql") {
        dependsOn("composeUp")
        doFirst {
            val port = dockerCompose.servicesInfos["postgresql"]?.port ?: 5432
            println("To connect to db, use the following commands:")
            println("export PGPASSWORD=kotlinkpass")
            println("psql -h 127.0.0.1 -p $port -U kotlinkuser kotlink")
        }
    }
}
