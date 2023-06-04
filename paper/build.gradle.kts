plugins {
    alias(libs.plugins.lombok)
    id("net.paradisu.platform-conventions")
}

dependencies {
    api(project(":core"))
    implementation(libs.dotenv.kotlin)
    implementation(libs.mariadb.java.client)
    implementation(libs.mysql.connector.java)
    implementation(libs.bundles.cloud.paper)
    implementation(libs.bundles.adventure.bukkit)
    implementation(libs.configurate.yaml)
    implementation(libs.inventorygui)
    implementation(libs.reflections)
    compileOnly(libs.connectorplugin.bukkit)
    compileOnly(libs.paper.api)
    compileOnly(libs.protocollib)
    compileOnly(libs.luckperms.api)
}

description = "paper"
