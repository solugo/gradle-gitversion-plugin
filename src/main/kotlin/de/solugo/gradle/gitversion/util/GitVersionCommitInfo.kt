package de.solugo.gradle.gitversion.util

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.revwalk.RevTag
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.gradle.api.Project
import org.semver.Version
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

data class GitVersionCommitInfo(
    val version: String,
    val hash: String,
    val timestamp: ZonedDateTime,
) {
    companion object {
        fun calculate(
            project: Project,
            base: String,
            qualifier: String,
            tagPattern: String?,
            majorPattern: String?,
            minorPattern: String?,
            patchPattern: String?,
        ) = run {
            val gitVersionTagRegex = (tagPattern?.takeUnless { it.isBlank() })?.toRegex()
            val gitVersionMajorRegex = (majorPattern?.takeUnless { it.isBlank() })?.toRegex()
            val gitVersionMinorRegex = (minorPattern?.takeUnless { it.isBlank() })?.toRegex()
            val gitVersionPatchRegex = (patchPattern?.takeUnless { it.isBlank() })?.toRegex()

            val gitRepository = FileRepositoryBuilder().run {
                findGitDir(project.rootDir)
                gitDir ?: error("Could not find git repository")
                build()
            }

            try {
                var version = Version.parse(base?.takeUnless { it.isBlank() } ?: "0.0.0")
                val head = gitRepository.resolve("HEAD") ?: error("Could not resolve HEAD")
                val walk = RevWalk(gitRepository).apply { markStart(parseCommit(head)) }

                val tagPrefix = "refs/tags/"
                val tags = LinkedHashMap<String, String>()
                val commits = LinkedHashMap<String, String>()

                val tagIterator = gitRepository.refDatabase.getRefsByPrefix(tagPrefix).iterator()

                while (tagIterator.hasNext()) {
                    val tag = tagIterator.next()
                    val hash = (walk.parseAny(tag.objectId) as? RevTag)?.`object`?.toObjectId()?.name ?: continue

                    tags[hash] = tag.name.removePrefix(tagPrefix)
                }

                val commitIterator = walk.iterator()

                while (commitIterator.hasNext()) {
                    val commit = commitIterator.next()
                    val hash = commit.toObjectId()?.name ?: continue

                    val tagVersion = tags[hash]?.let { gitVersionTagRegex?.matchEntire(it)?.groupValues?.last() }

                    if (tagVersion != null) {
                        version = Version.parse(tagVersion)
                        break
                    }

                    commits[hash] = commit.shortMessage
                }

                commits.values.reversed().forEach { message ->
                    when {
                        gitVersionMajorRegex?.matches(message) == true -> version = version.next(Version.Element.MAJOR)
                        gitVersionMinorRegex?.matches(message) == true -> version = version.next(Version.Element.MINOR)
                        gitVersionPatchRegex?.matches(message) == true -> version = version.next(Version.Element.PATCH)
                    }
                }

                val commit = gitRepository.parseCommit(head)
                val commitHash = commit.toObjectId().abbreviate(7).name()
                val commitTime = Instant.ofEpochSecond(commit.commitTime.toLong()).atZone(ZoneId.systemDefault())
                val modified = Git.wrap(gitRepository).use {
                    !it.status().call().isClean
                }

                when (qualifier) {
                    "hash" -> version = when {
                        modified -> version.next(Version.Element.PATCH).withQualifier("SNAPSHOT")
                        else -> version.withQualifier(commitHash)
                    }

                    "auto" -> version = when {
                        modified -> version.next(Version.Element.PATCH).withQualifier("SNAPSHOT")
                        else -> version.toReleaseVersion()
                    }
                }

                GitVersionCommitInfo(
                    version = version.toString(),
                    hash = commitHash,
                    timestamp = commitTime
                )
            } finally {
                gitRepository.close()
            }
        }

        private fun Version.withQualifier(value: String) = Version.parse("${toReleaseVersion()}-${value}")
    }

}