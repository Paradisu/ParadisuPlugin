rootProject.name = "ParadisuPlugin"
include(":bukkit")
include(":velocity")

pluginManagement {
    includeBuild("build-logic")
}