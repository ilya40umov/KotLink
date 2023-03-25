import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `jvm-test-suite`

    kotlin("jvm")

    id("com.avast.gradle.docker-compose")
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

dockerCompose {
    projectName = "kk-http4k"
    projectNamePrefix = "kk_http4k_"
    useComposeFiles.set(listOf("docker-compose.yaml", "docker-compose-local.yaml"))
    // when set to "false", the plugin automatically tries to reconnect to the containers from the previous run
    stopContainers.set(false)
}

testing {
    suites {
        named("test", JvmTestSuite::class) {
            useJUnitJupiter()
        }
    }
}

dependencies {
    implementation(platform("org.http4k:http4k-bom:4.19.0.0"))
    implementation("org.http4k:http4k-core")
    implementation("org.http4k:http4k-format-jackson")
    implementation("org.http4k:http4k-template-thymeleaf")
    implementation("org.http4k:http4k-security-oauth")
    implementation("org.http4k:http4k-client-apache")
    implementation("org.http4k:http4k-serverless-lambda")
    implementation("org.http4k:http4k-serverless-lambda-runtime")
    implementation("nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect:3.0.0")
    implementation("io.arrow-kt:arrow-core:1.0.1")
    implementation("commons-validator:commons-validator:1.7")
    implementation("com.auth0:java-jwt:3.18.3")
    implementation("com.auth0:jwks-rsa:0.20.1")
    implementation("software.amazon.awssdk:dynamodb:2.17.116")
    implementation("ch.qos.logback:logback-classic:1.2.9")
    "implementation"("org.slf4j:slf4j-api:1.7.33")
    implementation("org.slf4j:jcl-over-slf4j:1.7.33")
}

tasks {
    /* This task builds a Zip file which can be used to deploy to AWS Lambda on JVM. */
    register("buildLambdaZip", Zip::class) {
        from(compileKotlin)
        from(processResources)
        into("lib") {
            from(configurations.compileClasspath)
            from(configurations.runtimeClasspath)
        }
    }
    /* This task builds a JAR file which is then provided to GraalVM for creating a native image. */
    withType<ShadowJar>() {
        manifest.attributes["Main-Class"] = "org.kotlink.KotLinkFunctionKt"
        archiveBaseName.set(project.name)
        archiveClassifier.set("")
        archiveVersion.set("")
        mergeServiceFiles()
    }
}