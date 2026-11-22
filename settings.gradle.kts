include(":core")
include(":database")
include(":integration-tests")
include(":paper")
include(":velocity")


pluginManagement {
    repositories {
        gradlePluginPortal()
    }
    includeBuild("build-logic")
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version ("1.0.0")
}

// mcdevtools:begin
includeBuild("dev/java") { name = "mcdevtools" }
// mcdevtools:end
