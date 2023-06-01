plugins {
    id("net.paradisu.shadow-conventions")
}

dependencies {
    compileOnly(libs.configurate.yaml)
    compileOnly(libs.bundles.cloud.core)
    compileOnly(libs.bundles.adventure)
    compileOnly(libs.guava)
    compileOnly(libs.connectorplugin.core)
    compileOnly(libs.log4j.slf4j.impl)
}

description = "core"
