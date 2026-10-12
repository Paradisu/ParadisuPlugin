import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    `java-library`
    `maven-publish`
    id("com.diffplug.spotless")
}

// https://github.com/gradle/gradle/issues/15383
val libs = the<LibrariesForLibs>()

repositories {
    // mavenLocal()
    maven("https://repo.maven.apache.org/maven2/")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.opencollab.dev/main")
    maven("https://repo.minebench.de/")
}

// Use providers.gradleProperty(...) instead of properties[...]
group = providers.gradleProperty("group").get()
version = providers.gradleProperty("version").get()

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

spotless {
    java {
        palantirJavaFormat("2.83.0").formatJavadoc(true)
        importOrder("", "javax|java", "\\#")
        removeUnusedImports()
    }

    format("license") {
        licenseHeaderFile(rootProject.file("build-logic/src/main/resources/licenseHeaderFile"), "package ")
        target("src/main/java/**/*.java")
    }
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
}

tasks {
    processResources {
        filesMatching(listOf("plugin.yml", "velocity-plugin.json", "*.properties")) {
            expand(
                "id" to providers.gradleProperty("id").get(),
                "name" to providers.gradleProperty("pluginName").get(),
                "version" to providers.gradleProperty("version").get(),
                "paperApiVersion" to parseApiVersion(libs.versions.paper.api.get()),
                "description" to providers.gradleProperty("description").get(),
                "url" to providers.gradleProperty("url").get(),
                "authors" to providers.gradleProperty("authors").get()
            )
        }
    }
}

fun parseApiVersion(input: String): String {
    val regex = Regex("""^(\d+\.\d+(\.\d+)?)""")
    val matchResult = regex.find(input)
    val version = matchResult?.groupValues?.get(1)

    return version ?: "unknown"
}
