plugins {
    id("net.paradisu.java-conventions")
    application
}

dependencies {
    implementation(project(":database"))
    implementation(libs.bundles.liquibase)
    runtimeOnly(libs.slf4j.simple)
}

application {
    mainClass.set("net.paradisu.devtools.DevMigrate")
}

description = "migrate"

// `dev` only uses installDist
tasks {
    distZip { enabled = false }
    distTar { enabled = false }
}
