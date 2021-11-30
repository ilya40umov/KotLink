import com.avast.gradle.dockercompose.ComposeSettings
import org.springframework.boot.gradle.tasks.bundling.BootJar
import org.springframework.boot.gradle.tasks.run.BootRun

plugins {
    kotlin("jvm")
    kotlin("kapt")
    kotlin("plugin.spring")
    kotlin("plugin.allopen")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("com.avast.gradle.docker-compose")
}

dockerCompose {
    projectName = "kotlink"
    projectNamePrefix = "kotlink_"
    useComposeFiles.set(listOf("docker-compose.yaml", "docker-compose-local.yaml"))

    createNested("test").apply {
        projectName = "kotlink-ci"
        projectNamePrefix = "kotlink_ci_"
        useComposeFiles.set(listOf("docker-compose.yaml", "docker-compose-ci.yaml"))
        forceRecreate.set(true)
    }
}

tasks {
    fun ComposeSettings.postgresUrl() =
        "jdbc:postgresql://localhost:${servicesInfos["postgresql"]?.port ?: 5432}/kotlink"

    fun ComposeSettings.redisUrl() =
        "redis://localhost:${servicesInfos["redis"]?.port ?: 6379}"

    withType<Test> {
        dependsOn("testComposeUp")
        finalizedBy("testComposeDown")
        useJUnitPlatform {}
        doFirst {
            systemProperty("spring.datasource.url", dockerCompose.nested("test").postgresUrl())
            systemProperty("spring.redis.url", dockerCompose.nested("test").redisUrl())
        }
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

dependencies {

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.springframework.session:spring-session-jdbc")
    implementation("org.springframework.session:spring-session-data-redis")
    implementation("org.springframework.boot:spring-boot-devtools")

    compileOnly("org.springframework.boot:spring-boot-configuration-processor")
    kapt("org.springframework.boot:spring-boot-configuration-processor")

    implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity5")

    implementation("com.github.ben-manes.caffeine:caffeine")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.github.microutils:kotlin-logging:2.1.0")
    implementation("org.slf4j:slf4j-api:1.7.32")

    val logbackVersion = "1.2.7"
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("ch.qos.logback:logback-core:$logbackVersion")
    implementation("ch.qos.logback:logback-access:$logbackVersion")

    implementation("org.apache.logging.log4j:log4j-to-slf4j:2.14.1")
    implementation("org.postgresql:postgresql:42.3.1")
    implementation("org.flywaydb:flyway-core:8.2.0")

    val exposedVersion = "0.36.2"
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:spring-transaction:$exposedVersion")

    implementation("nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect:3.0.0")

    val jupiterVersion = "5.8.2"

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$jupiterVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$jupiterVersion")
    testImplementation("org.amshove.kluent:kluent:1.68")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
    testImplementation("org.mockito:mockito-junit-jupiter:4.1.0")
}
