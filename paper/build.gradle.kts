plugins {
    alias(libs.plugins.lombok)
    id("net.paradisu.platform-conventions")
}

dependencies {
    api(project(":core"))
    implementation(libs.dotenv.kotlin)
    implementation(libs.bundles.cloud.paper)
    implementation(libs.bundles.adventure.bukkit)
    implementation(libs.bundles.database)
    implementation(libs.configurate.yaml)
    implementation(libs.inventorygui)
    compileOnly(libs.paper.api)
    compileOnly(libs.connectorplugin.bukkit)
    compileOnly(libs.protocollib)
    compileOnly(libs.luckperms.api)
}

description = "paper"
