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
    implementation(platform("org.http4k:http4k-bom:4.17.2.0"))
    implementation("org.http4k:http4k-core")
    implementation("org.http4k:http4k-template-thymeleaf")
    implementation("org.http4k:http4k-serverless-lambda")

    implementation("nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect:3.0.0")
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