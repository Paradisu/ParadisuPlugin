plugins {
    id("net.paradisu.java-conventions")
}

dependencies {
    compileOnly(libs.paper.api)
    compileOnly(libs.velocity.api)
    compileOnly(libs.bundles.netty)
    compileOnly(libs.gson)
    // Limbo's pom pulls jitpack-only dependencies we never touch; the Limbo jar alone is enough to compile against
    compileOnly(libs.limbo) { isTransitive = false }
}

tasks.jar {
    archiveFileName.set("ParadisuCapture.jar")
}

description = "capture"
