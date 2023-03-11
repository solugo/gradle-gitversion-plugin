package de.solugo.gradle.gitversion.task

import de.solugo.gradle.gitversion.extension.GitVersionExtension
import de.solugo.gradle.gitversion.util.GitVersionBuildInfo
import org.gradle.api.DefaultTask
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction


open class GitVersionCreatePropertiesFileTask : DefaultTask() {

    @Internal
    override fun getGroup(): String = "GitVersion"

    @Internal
    override fun getDescription(): String = "Create gitVersion properties file"

    @Input
    val buildInfo = project.objects.property(GitVersionBuildInfo::class.java).value(
        project.provider {
            val extension = project.extensions.getByType(GitVersionExtension::class.java)
            extension.buildInfo
        }
    )

    @OutputFile
    val outputFile = project.objects.fileProperty().value {
        val extension = project.extensions.getByType(JavaPluginExtension::class.java)
        val sourceSet = extension.sourceSets.getByName("main")
        val resourceDirectory = checkNotNull(sourceSet.output.resourcesDir)

        resourceDirectory.resolve("gitVersion.properties")
    }

    @TaskAction
    fun execute() {
        buildInfo.get().apply {
            outputFile.get().asFile.writeText(
                """
                version=${version}
                timestamp=${timestamp}
                git.hash=${gitHash ?: ""}
                git.timestamp=${gitTimestamp ?: ""}
                """.trimIndent()
            )
        }
    }

}