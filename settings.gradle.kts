include(":core")
include(":database")
include(":paper")
include(":velocity")


pluginManagement {
    repositories {
        gradlePluginPortal()
    }
    includeBuild("build-logic")
}