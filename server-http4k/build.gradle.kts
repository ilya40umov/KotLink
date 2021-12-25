plugins {
    `jvm-test-suite`

    kotlin("jvm")
}

testing {
    suites {
        named("test", JvmTestSuite::class) {
            useJUnitJupiter()
        }
    }
}

dependencies {
    implementation(platform("org.http4k:http4k-bom:4.17.6.0"))
    implementation("org.http4k:http4k-core")
    implementation("org.http4k:http4k-template-thymeleaf")
    implementation("org.http4k:http4k-security-oauth")
    implementation("org.http4k:http4k-client-apache")
    implementation("org.http4k:http4k-serverless-lambda")
    implementation("nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect:3.0.0")
    implementation("io.arrow-kt:arrow-core:1.0.1")
    implementation("commons-validator:commons-validator:1.7")
    implementation("com.auth0:java-jwt:3.18.2")
    implementation("com.auth0:jwks-rsa:0.20.0")
    implementation("software.amazon.awssdk:dynamodb:2.17.102")
}

tasks {
    register("buildLambdaZip", Zip::class) {
        from(compileKotlin)
        from(processResources)
        into("lib") {
            from(configurations.compileClasspath)
        }
    }
}