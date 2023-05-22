rootProject.name = "ParadisuPlugin"
include(":core")
include(":bukkit")
include(":velocity")


pluginManagement {
    includeBuild("build-logic")
}