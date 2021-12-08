plugins {
    `jvm-test-suite`

    kotlin("jvm")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
        }
    }
}

dependencies {

    val http4kVersion = "4.17.2.0"
    implementation("org.http4k:http4k-core:$http4kVersion")
    implementation("org.http4k:http4k-template-thymeleaf:$http4kVersion")
    implementation("org.http4k:http4k-serverless-lambda:$http4kVersion")

    implementation("nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect:3.0.0")

    // TODO: move to log4j2
    val logbackVersion = "1.2.7"
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("ch.qos.logback:logback-core:$logbackVersion")
    implementation("ch.qos.logback:logback-access:$logbackVersion")
    implementation("org.slf4j:slf4j-api:1.7.32")

    val jupiterVersion = "5.8.2"
    testImplementation("org.junit.jupiter:junit-jupiter-api:$jupiterVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$jupiterVersion")

    testImplementation("org.amshove.kluent:kluent:1.68")
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