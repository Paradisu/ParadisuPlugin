plugins {
    alias(libs.plugins.lombok)
    id("net.paradisu.platform-conventions")
}

dependencies {
    api(project(":core"))
    implementation(libs.bundles.cloud.velocity)
    implementation(libs.bundles.adventure.velocity)
    implementation(libs.bundles.fastutil)
    implementation(libs.bundles.liquibase)
    implementation(libs.configurate.yaml)
    implementation(libs.guava)
    compileOnly(libs.velocity.api)
    compileOnly(libs.connectorplugin.velocity)
    compileOnly(libs.luckperms.api)
}

description = "velocity"
