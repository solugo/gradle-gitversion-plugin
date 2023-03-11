package de.solugo.gradle.gitversion.task

import de.solugo.gradle.gitversion.extension.GitVersionExtension
import de.solugo.gradle.gitversion.util.GitVersionBuildInfo
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction


open class GitVersionCreateVersionFileTask : DefaultTask() {

    @Internal
    override fun getGroup(): String = "GitVersion"

    @Internal
    override fun getDescription(): String = "Create gitVersion version file"

    @Input
    val buildInfo = project.objects.property(GitVersionBuildInfo::class.java).value(
        project.provider {
            val extension = project.extensions.getByType(GitVersionExtension::class.java)
            extension.buildInfo
        }
    )

    @OutputFile
    val outputFile = project.objects.fileProperty().value {
        project.buildDir.resolve("VERSION")
    }

    @TaskAction
    fun execute() {
        outputFile.get().asFile.writeText(buildInfo.get().version)
    }

}