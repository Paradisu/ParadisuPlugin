import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("net.paradisu.java-conventions")
    id("com.gradleup.shadow")
}

tasks {
    shadowJar {
        archiveClassifier.set("")
        // relocate("some.class.path", "net.paradisu.libs")
    }

    build {
        dependsOn(shadowJar)
    }
}