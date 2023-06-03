include(":core")
include(":bukkit")
include(":velocity")


pluginManagement {
    repositories {
        gradlePluginPortal()
    }
    includeBuild("build-logic")
}