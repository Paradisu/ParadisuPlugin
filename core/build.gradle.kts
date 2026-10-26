import org.eclipse.jgit.api.Git
import org.eclipse.jgit.revwalk.RevWalk
import net.kyori.indra.git.RepositoryValueSource
import net.kyori.blossom.BlossomExtension
import groovy.util.Node

plugins {
    alias(libs.plugins.blossom)
    alias(libs.plugins.indra.git)
    alias(libs.plugins.lombok)
    id("net.paradisu.java-conventions")
    `eclipse`
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

abstract class CommitMessageValueSource : RepositoryValueSource.Parameterless<String>() {
    override fun obtain(repository: Git): String? {
        val headCommitId = repository.repository.resolve("HEAD")

        if (headCommitId == null) {
            return ""
        }

        RevWalk(repository.repository).use { walk ->
            val commit = walk.parseCommit(headCommitId)
            return commit.fullMessage
        }
    }
}

abstract class RepositoryUrlValueSource : RepositoryValueSource.Parameterless<String>() {
    override fun obtain(repository: Git): String? {
        return repository.repository.config.getString("remote", "origin", "url")
    }
}

val gitBranch = indraGit.branchName().orElse("DEV")
val gitCommit = indraGit.commit()

val gitCommitName = gitCommit.map { it?.name ?: "0".repeat(40) }
val gitCommitAbbrev = gitCommit.map { it?.name?.substring(0, 7) ?: "0".repeat(7) }

val gitCommitMessage = indraGit.repositoryValue(CommitMessageValueSource::class.java).orElse("")
val gitRepositoryUrl = indraGit.repositoryValue(RepositoryUrlValueSource::class.java).orElse("").map {
    it.replace("git@github.com:", "https://github.com/")
}

sourceSets {
    main {
        blossom {
            javaSources {
                property("plugin.id", providers.gradleProperty("id").get())
                property("plugin.name", providers.gradleProperty("pluginName").get())
                property("plugin.version", providers.gradleProperty("version").get())
                property("plugin.description", providers.gradleProperty("description").get())
                property("plugin.url", providers.gradleProperty("url").get())
                property("plugin.authors", providers.gradleProperty("authors").get())
                
                property("git.branch", gitBranch)
                property("git.commit", gitCommitName)
                property("git.commitAbbrev", gitCommitAbbrev)
                property("git.buildNumber", providers.environmentVariable("GITHUB_RUN_NUMBER").orElse("-1"))
                property("git.commitMessage", gitCommitMessage)
                property("git.repository", gitRepositoryUrl)
            }
        }
    }
}

fun isDevBuild(branch: String, repository: String): Boolean {
    return branch != "master" || repository.equals("https://github.com/Paradisu/ParadisuPlugin", ignoreCase = true).not()
}

eclipse {
    classpath {
        file {
            withXml {
                val node = asNode()
                val removed = node.children().removeAll { child: Any? ->
                    if (child !is Node) return@removeAll false
                    if (child.name() != "classpathentry") return@removeAll false
                    val attrs = child.attributes()
                    attrs["kind"] == "src" && attrs["path"] == "build/sources/java"
                }
            }
        }
    }
}

description = "core"
