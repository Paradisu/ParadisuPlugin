plugins {
    id("org.gradle.kotlin.kotlin-dsl") version "6.7.6"
}

dependencies {
    implementation(libs.shadow)
    implementation(libs.spotless)
    // https://github.com/gradle/gradle/issues/15383
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}