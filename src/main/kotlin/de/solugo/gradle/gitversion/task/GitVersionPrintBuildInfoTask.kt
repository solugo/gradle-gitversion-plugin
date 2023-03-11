package de.solugo.gradle.gitversion.task

import de.solugo.gradle.gitversion.extension.GitVersionExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction


open class GitVersionPrintBuildInfoTask: DefaultTask() {

    @Internal
    override fun getGroup(): String = "GitVersion"
    @Internal
    override fun getDescription(): String = "Print gitVersion build info"

    @TaskAction
    fun execute() {
        val extension = project.extensions.getByType(GitVersionExtension::class.java)
        project.logger.lifecycle("Version: ${extension.buildInfo.version}")
        project.logger.lifecycle("Timestamp: ${extension.buildInfo.timestamp}")
        project.logger.lifecycle("GitHash: ${extension.buildInfo.gitHash}")
        project.logger.lifecycle("GitTimestamp: ${extension.buildInfo.gitTimestamp}")
    }
}