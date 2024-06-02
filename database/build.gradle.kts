plugins {
    alias(libs.plugins.lombok)
    id("net.paradisu.java-conventions")
}

dependencies {
    compileOnly(libs.bundles.database)
}