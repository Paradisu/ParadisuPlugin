plugins {
    alias(libs.plugins.lombok)
    id("net.paradisu.java-conventions")
}

dependencies {
    implementation(libs.bundles.database)
}