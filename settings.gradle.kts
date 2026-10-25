include(":core")
include(":database")
include(":paper")
include(":velocity")
include(":devtools")
include(":devtools:capture")
include(":devtools:migrate")


pluginManagement {
    repositories {
        gradlePluginPortal()
    }
    includeBuild("build-logic")
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version ("1.0.0")
}
