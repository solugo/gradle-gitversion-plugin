package de.solugo.gradle.gitversion.util

import java.nio.file.Paths

object GitVersionPipeline {

    private val modifiers = listOf(
        PipelineModifier(
            name = "azure",
            modify = {
                if (!environment.containsKey("BUILD_BUILDID")) return@PipelineModifier false

                out("##vso[build.updatebuildnumber]${buildInfo.version}")
                out("##vso[task.setvariable variable=BUILD_VERSION]${buildInfo.version}")
                out("##vso[task.setvariable variable=BUILD_TIMESTAMP]${buildInfo.timestamp}")
                out("##vso[task.setvariable variable=GIT_HASH]${buildInfo.gitHash}")
                out("##vso[task.setvariable variable=GIT_TIMESTAMP]${buildInfo.gitTimestamp}")

                true
            }
        ),
        PipelineModifier(
            name = "github",
            modify = {
                val env = environment["GITHUB_ENV"] ?: return@PipelineModifier false

                Paths.get(env).toFile().writer().use { writer ->
                    writer.appendLine("BUILD_VERSION=${buildInfo.version}")
                    writer.appendLine("BUILD_TIMESTAMP=${buildInfo.timestamp}")
                    writer.appendLine("GIT_HASH=${buildInfo.gitHash}")
                    writer.appendLine("GIT_TIMESTAMP=${buildInfo.gitTimestamp}")
                }
                true
            }
        ),
    )

    fun modify(
        selection: String = "auto",
        out: (String) -> Unit = System.out::println,
        environment: Map<String, String> = System.getenv(),
        buildInfo: GitVersionBuildInfo,
    ) = run {
        val context = PipelineModifierContext(
            out = out,
            environment = environment,
            buildInfo = buildInfo,
        )

        modifiers.associate { modifier ->
            modifier.name to when {
                (selection == "auto" || selection == modifier.name) -> modifier.modify(context)
                else -> false
            }
        }
    }

    private data class PipelineModifier(
        val name: String,
        val modify: PipelineModifierContext.() -> Boolean,
    )

    private data class PipelineModifierContext(
        val out: (String) -> Unit = System.out::println,
        val environment: Map<String, String>,
        val buildInfo: GitVersionBuildInfo,
    )

}