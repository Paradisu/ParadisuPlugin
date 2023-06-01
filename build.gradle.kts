plugins {
    id("net.paradisu.java-conventions")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    `java-library`
}

dependencies {
    implementation(project(":bukkit"))
    implementation(project(":velocity"))
}

tasks {
    val shadowJar by existing(com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar::class) {
        archiveClassifier.set("")
        archiveFileName.set("ParadisuPlugin.jar")
    }

    build {
        dependsOn(shadowJar)
    }
}