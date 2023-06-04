plugins {
    id("net.paradisu.java-conventions")
    alias(libs.plugins.shadow)
    `java-library`
}

dependencies {
    implementation(project(":paper"))
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

tasks.named("shadowJar") {
    this.dependsOn(":core:shadowJar")
    this.dependsOn(":paper:shadowJar")
    this.dependsOn(":velocity:shadowJar")
}