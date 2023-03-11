package de.solugo.gradle.gitversion.extension

import de.solugo.gradle.gitversion.util.GitVersionBuildInfo
import de.solugo.gradle.gitversion.util.GitVersionCommitInfo
import de.solugo.gradle.gitversion.util.GitVersionPipeline
import org.gradle.api.GradleException
import org.gradle.api.Project
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class GitVersionExtension(
    project: Project,
) {
    private val properties = project.properties as Map<String, Any?>
    private val gitVersionEnabled: Boolean by properties.withDefault { true }
    private val gitVersionTrigger: String by properties.withDefault { "git" }
    private val gitVersionPipeline: String by properties.withDefault { "auto" }
    private val gitVersionBase: String by properties.withDefault { "0.0.0" }
    private val gitVersionQualifier: String by properties.withDefault { "auto" }
    private val gitVersionTagPattern: String by properties.withDefault { "^v(.+)$" }
    private val gitVersionMajorPattern: String by properties.withDefault { "" }
    private val gitVersionMinorPattern: String by properties.withDefault { "" }
    private val gitVersionPatchPattern: String by properties.withDefault { "^(.*)$" }

    val commitInfo = try {
        when {
            gitVersionEnabled && project.version.toString() == gitVersionTrigger -> GitVersionCommitInfo.calculate(
                project = project,
                base = gitVersionBase,
                qualifier = gitVersionQualifier,
                tagPattern = gitVersionTagPattern,
                majorPattern = gitVersionMajorPattern,
                minorPattern = gitVersionMinorPattern,
                patchPattern = gitVersionPatchPattern,
            )

            else -> null
        }
    } catch (ex: Exception) {
        throw GradleException("Could not calculate git version", ex)
    }

    val buildInfo = GitVersionBuildInfo(
        version = commitInfo?.version ?: project.version.toString(),
        timestamp = ZonedDateTime.now().format(),
        gitHash = commitInfo?.hash,
        gitTimestamp = commitInfo?.timestamp?.format(),
    )

    val pipelineModifications  = GitVersionPipeline.modify(
        selection = gitVersionPipeline,
        buildInfo = buildInfo,
    )
    private fun ZonedDateTime.format() = truncatedTo(ChronoUnit.SECONDS).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
}