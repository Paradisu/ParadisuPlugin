plugins {
    id("net.paradisu.shadow-conventions")
}

tasks.named("shadowJar") {
    this.dependsOn(":core:shadowJar")
}