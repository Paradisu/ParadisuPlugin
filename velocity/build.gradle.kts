plugins {
    id("net.paradisu.shadow-conventions")
}

dependencies {
    implementation("cloud.commandframework:cloud-velocity:1.8.3")
    implementation("cloud.commandframework:cloud-minecraft-extras:1.8.3")
    implementation("net.kyori:adventure-api:4.13.1-SNAPSHOT")
    implementation("net.kyori:adventure-text-minimessage:4.13.1-SNAPSHOT")
    implementation("org.spongepowered:configurate-yaml:4.0.0")
    implementation("com.google.guava:guava:31.1-jre")
    compileOnly("com.velocitypowered:velocity-api:3.0.1")
    compileOnly("de.themoep.connectorplugin:velocity:1.5-SNAPSHOT")
    compileOnly("net.luckperms:api:5.4")
}

description = "velocity"
