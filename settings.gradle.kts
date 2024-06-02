include(":core")
include(":database")
include(":liquibase")
include(":paper")
include(":velocity")


pluginManagement {
    repositories {
        gradlePluginPortal()
    }
    includeBuild("build-logic")
}