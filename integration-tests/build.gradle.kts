plugins {
    id("net.paradisu.java-conventions")
}

// Integration tests run against the running dev network, so they are not part of `./gradlew build`:
// `dev test run` starts the network, runs this suite and collects the results (see AGENTS.md).
testing {
    suites {
        register<JvmTestSuite>("integrationTest") {
            useJUnitJupiter(libs.versions.junit)
            dependencies {
                // The mcdevtools testkit (included build `mcdevtools`, see settings.gradle.kts): bots, JUnit extension
                implementation("net.paradisu.mcdevtools:testkit")
            }
            targets.all {
                testTask.configure {
                    // They depend on a live network, never on Gradle's idea of up to date
                    outputs.upToDateWhen { false }
                    testLogging {
                        events("passed", "failed", "skipped")
                        showStandardStreams = false
                    }
                }
            }
        }
    }
}
