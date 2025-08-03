plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.shadow)
    implementation(libs.spotless)
    // https://github.com/gradle/gradle/issues/15383
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}