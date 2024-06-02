plugins {
    alias(libs.plugins.liquibase)
    id("net.paradisu.java-conventions")
}

dependencies {
    api(project(":database"))
    compileOnly(libs.bundles.database)
    compileOnly(libs.bundles.liquibase)
    liquibaseRuntime(libs.bundles.liquibase)
    liquibaseRuntime(sourceSets.getByName("main").output)
}

liquibase {
    activities.register("main") {
        val coreProjectDir = project(":core").projectDir
        arguments = mapOf(
                "changelogFile" to "$coreProjectDir/src/main/resources/db/changelog/db.changelog-master.yaml",
                "classpath" to "$coreProjectDir/src/main/resources",
                "url" to (project.findProperty("liquibaseJdbcUrl") ?: "jdbc:postgresql://localhost:5432/paradisu"),
                "referenceUrl" to "hibernate:classic:net.paradisu.liquibase.Liquibase",
                "username" to (project.findProperty("liquibaseUser") ?: "paradisu"),
                "password" to (project.findProperty("liquibasePassword") ?: "paradisu"),
                "logLevel" to "info"
        )
    }
}

configurations {
    liquibaseRuntime {
        extendsFrom(configurations.compileClasspath.get())
    }
}

tasks.withType<org.liquibase.gradle.LiquibaseTask>().configureEach {
    dependsOn(tasks.named("build"))
}