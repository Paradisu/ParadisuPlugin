plugins {
    id("net.paradisu.java-conventions")
    alias(libs.plugins.shadow)
    `java-library`
    `eclipse`
}

dependencies {
    implementation(project(":paper"))
    implementation(project(":velocity"))
}

tasks {
    shadowJar {
        archiveClassifier.set("")
        archiveFileName.set("ParadisuPlugin.jar")
    }

    build {
        dependsOn(shadowJar)
    }
}
