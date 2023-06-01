plugins {
    id("net.paradisu.platform-conventions")
}

dependencies {
    implementation(libs.dotenv.kotlin)
    implementation(libs.mariadb.java.client)
    implementation(libs.mysql.connector.java)
    implementation(libs.bundles.cloud.paper)
    annotationProcessor(libs.cloud.annotations)
    implementation(libs.inventorygui)
    compileOnly(libs.spigot.api)
    compileOnly(libs.protocollib)
    compileOnly(libs.luckperms.api)
}

description = "bukkit"
