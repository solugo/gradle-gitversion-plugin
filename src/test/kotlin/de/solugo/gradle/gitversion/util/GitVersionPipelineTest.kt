package de.solugo.gradle.gitversion.util

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

class GitVersionPipelineTest {


    private val buildInfo = GitVersionBuildInfo(
        version = "1.2.3",
        timestamp = "build-timestamp",
        gitHash = "abcdefgh",
        gitTimestamp = "git-timestamp",
    )

    @Test
    fun `apply azure modification`() {
        val output = StringWriter().run {
            GitVersionPipeline.modify(
                out = PrintWriter(this)::println,
                buildInfo = buildInfo,
                environment = mapOf(
                    "BUILD_BUILDID" to "dummy",
                ),
            )

            toString()
        }

        Assertions.assertThat(output).contains("##vso[build.updatebuildnumber]1.2.3")
        Assertions.assertThat(output).contains("##vso[task.setvariable variable=BUILD_VERSION]1.2.3")
        Assertions.assertThat(output).contains("##vso[task.setvariable variable=BUILD_TIMESTAMP]build-timestamp")
        Assertions.assertThat(output).contains("##vso[task.setvariable variable=GIT_HASH]abcdefgh")
        Assertions.assertThat(output).contains("##vso[task.setvariable variable=GIT_TIMESTAMP]git-timestamp")

    }

    @Test
    fun `apply github modification`() {
        val file = File.createTempFile("gitVersionPiplineEnv", ".out")
        val output = StringWriter().run {
            GitVersionPipeline.modify(
                out = PrintWriter(this)::println,
                buildInfo = buildInfo,
                environment = mapOf(
                    "GITHUB_ENV" to file.absolutePath,
                ),
            )

            toString()
        }

        val fileContent = file.readText()

        Assertions.assertThat(output).isEmpty()
        Assertions.assertThat(fileContent).contains("BUILD_VERSION=1.2.3")
        Assertions.assertThat(fileContent).contains("BUILD_TIMESTAMP=build-timestamp")
        Assertions.assertThat(fileContent).contains("GIT_HASH=abcdefgh")
        Assertions.assertThat(fileContent).contains("GIT_TIMESTAMP=git-timestamp")

    }

}