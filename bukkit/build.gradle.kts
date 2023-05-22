plugins {
    id("net.paradisu.shadow-conventions")
}

dependencies {
    implementation("io.github.cdimascio:dotenv-kotlin:6.2.2")
    implementation("org.mariadb.jdbc:mariadb-java-client:2.3.0")
    implementation("mysql:mysql-connector-java:8.0.28")
    implementation("cloud.commandframework:cloud-paper:1.8.3")
    implementation("cloud.commandframework:cloud-annotations:1.8.3")
    annotationProcessor("cloud.commandframework:cloud-annotations:1.8.3")
    implementation("de.themoep:inventorygui:1.5.1-SNAPSHOT")
    compileOnly("org.spigotmc:spigot-api:1.19.4-R0.1-SNAPSHOT")
    compileOnly("com.comphenix.protocol:ProtocolLib:5.0.0-SNAPSHOT")
    compileOnly("net.luckperms:api:5.4")
}

description = "bukkit"
