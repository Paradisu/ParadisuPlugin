import net.kyori.blossom.BlossomExtension

plugins {
    alias(libs.plugins.blossom)
    alias(libs.plugins.indra.git)
    alias(libs.plugins.lombok)
    id("net.paradisu.shadow-conventions")
}

dependencies {
    api(project(":database"))
    compileOnly(libs.bundles.adventure.core)
    compileOnly(libs.bundles.cloud.core)
    compileOnly(libs.bundles.database)
    compileOnly(libs.bundles.fastutil)
    compileOnly(libs.bundles.liquibase)
    compileOnly(libs.configurate.yaml)
    compileOnly(libs.connectorplugin.core)
    compileOnly(libs.guava)
    compileOnly(libs.log4j.slf4j.impl)
}

// Mostly mirrors Geyser's setup https://github.com/GeyserMC/Geyser/blob/master/core/build.gradle.kts#L87-L134
configure<BlossomExtension> {
    val constantsFile = "src/main/java/net/paradisu/core/utils/Constants.java"
    val info = GitInfo()

    replaceToken("\${plugin.id}", properties["id"], constantsFile)
    replaceToken("\${plugin.name}", properties["pluginName"], constantsFile)
    replaceToken("\${plugin.version}", properties["version"], constantsFile)
    replaceToken("\${plugin.description}", properties["description"], constantsFile)
    replaceToken("\${plugin.url}", properties["url"], constantsFile)
    replaceToken("\${plugin.authors}", properties["authors"], constantsFile)

    replaceToken("\${git.branch}", info.branch, constantsFile)
    replaceToken("\${git.commit}", info.commit, constantsFile)
    replaceToken("\${git.commitAbbrev}", info.commitAbbrev, constantsFile)
    replaceToken("\${git.buildNumber}", info.buildNumber, constantsFile)
    replaceToken("\${git.commitMessage}", info.commitMessage, constantsFile)
    replaceToken("\${git.repository}", info.repository, constantsFile)
}

inner class GitInfo {
    val branch: String
    val commit: String
    val commitAbbrev: String

    val buildNumber: String

    val commitMessage: String
    val repository: String

    
    init {
        branch = indraGit.branchName() ?: System.getenv("BRANCH_NAME") ?: "DEV"

        val commit = indraGit.commit()
        this.commit = commit?.name ?: "0".repeat(40)
        commitAbbrev = commit?.name?.substring(0, 7) ?: "0".repeat(7)

        buildNumber = (System.getenv("GITHUB_RUN_NUMBER")) ?: "-1"

        val git = indraGit.git()
        commitMessage = git?.commit()?.message ?: ""
        repository = git?.repository?.config?.getString("remote", "origin", "url") ?: ""
    }
}

description = "core"
