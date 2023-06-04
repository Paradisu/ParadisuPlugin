import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("net.paradisu.java-conventions")
    id("com.github.johnrengelman.shadow")
}

tasks {
    shadowJar {
        archiveClassifier.set("")
        relocate("de.themoep.inventorygui", "net.paradisu.libs")
    }

    build {
        dependsOn(shadowJar)
    }
}