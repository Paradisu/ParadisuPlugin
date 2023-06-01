plugins {
    id("net.paradisu.platform-conventions")
}

dependencies {
    api(project(":core"))
    implementation(libs.bundles.cloud.velocity)
    implementation(libs.bundles.adventure)
    implementation(libs.configurate.yaml)
    implementation(libs.guava)
    compileOnly(libs.velocity.api)
    compileOnly(libs.connectorplugin.velocity)
    compileOnly(libs.luckperms.api)
}

description = "velocity"
