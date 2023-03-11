package de.solugo.gradle.gitversion

import de.solugo.gradle.gitversion.extension.GitVersionExtension
import de.solugo.gradle.gitversion.task.GitVersionCreatePropertiesFileTask
import de.solugo.gradle.gitversion.task.GitVersionCreateVersionFileTask
import de.solugo.gradle.gitversion.task.GitVersionPrintBuildInfoTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class GitVersionPlugin : Plugin<Project> {
    override fun apply(project: Project) {

        val extensions = project.rootProject.extensions
        val extension = extensions.findByType(GitVersionExtension::class.java) ?: GitVersionExtension(project)

        project.extensions.add("gitVersion", extension)
        project.version = extension.buildInfo.version
        project.tasks.register("gitVersionPrintBuildInfo", GitVersionPrintBuildInfoTask::class.java)
        project.tasks.register("gitVersionCreatePropertiesFile", GitVersionCreatePropertiesFileTask::class.java)
        project.tasks.register("gitVersionCreateVersionFile", GitVersionCreateVersionFileTask::class.java)

        project.tasks

    }

}