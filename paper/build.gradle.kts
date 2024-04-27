plugins {
    alias(libs.plugins.lombok)
    id("net.paradisu.platform-conventions")
}

dependencies {
    api(project(":core"))
    implementation(libs.dotenv.kotlin)
    implementation(libs.mariadb.java.client)
    implementation(libs.mysql.connector.j)
    implementation(libs.bundles.cloud.paper)
    implementation(libs.bundles.adventure.bukkit)
    implementation(libs.configurate.yaml)
    implementation(libs.inventorygui)
    compileOnly(libs.connectorplugin.bukkit)
    compileOnly(libs.paper.api)
    compileOnly(libs.protocollib)
    compileOnly(libs.luckperms.api)
}

description = "paper"
