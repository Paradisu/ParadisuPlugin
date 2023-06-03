import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    `java-library`
    `maven-publish`
}

// https://github.com/gradle/gradle/issues/15383
val libs = the<LibrariesForLibs>()

repositories {
    mavenLocal()
    maven("https://repo.maven.apache.org/maven2/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") {
        mavenContent { snapshotsOnly() }
    }
    maven("https://repo.dmulloy2.net/repository/public/")
    maven("https://repo.minebench.de/")
    maven("https://oss.sonatype.org/content/repositories/snapshots") {
        mavenContent { snapshotsOnly() }
    }
    maven("https://repo.velocitypowered.com/snapshots/") {
        mavenContent { snapshotsOnly() }
    }
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/") {
        mavenContent { snapshotsOnly() }
    }
    maven("https://repo.spongepowered.org/repository/maven-public/")
}

group = properties["group"] as String
version = properties["version"] as String
java.sourceCompatibility = JavaVersion.VERSION_17

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc>() {
    options.encoding = "UTF-8"
}

tasks {
    processResources {
        filesMatching(listOf("plugin.yml", "velocity-plugin.json", "*_en.properties")) {
            expand(
                "id" to properties["id"],
                "name" to properties["pluginName"],
                "version" to properties["version"],
                "spigotApiVersion" to parseApiVersion(libs.versions.spigot.api.toString()),
                "description" to properties["description"],
                "url" to properties["url"],
                "authors" to properties["authors"]
            )
        }
    }
}

fun parseApiVersion(input: String): String {
    val regex = Regex("""^(\d+\.\d+)""")
    val matchResult = regex.find(input)
    val version = matchResult?.groupValues?.get(1)

    return version ?: "unknown"
}