package de.solugo.gradle.gitversion

import org.eclipse.jgit.lib.IndexDiff
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevTag
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.treewalk.FileTreeIterator
import org.gradle.api.Project

open class GitVersionExtension(
    private val project: Project
) {
    val override = project.objects.property(String::class.java).apply {
        set(project.provider { project.properties["gitVersionOverride"]?.toString() })
    }
    val base = project.objects.property(String::class.java).apply {
        set(project.provider { project.properties["gitVersionBase"]?.toString() ?: "0.0.0" })
    }
    val qualifier = project.objects.property(String::class.java).apply {
        set(project.provider { project.properties["gitVersionQualifier"]?.toString() })
    }
    val tagPrefix = project.objects.property(String::class.java).apply {
        set(project.provider { project.properties["gitVersionTagPrefix"]?.toString() })
    }
    val majorCommitPattern = project.objects.property(String::class.java).apply {
        set(project.provider { project.properties["gitVersionMajorCommitPattern"]?.toString() })
    }
    val minorCommitPattern = project.objects.property(String::class.java).apply {
        set(project.provider { project.properties["gitVersionMinorCommitPattern"]?.toString() })
    }
    val patchCommitPattern = project.objects.property(String::class.java).apply {
        set(project.provider { project.properties["gitVersionPatchCommitPattern"]?.toString() ?: ".*" })
    }

    val version; get() = version()

    @JvmOverloads
    fun version(
        override: String? = this.override.orNull,
        base: String? = this.base.orNull,
        qualifier: String? = this.qualifier.orNull,
        tagPrefix: String? = this.tagPrefix.orNull,
        majorCommitPattern: String? = this.majorCommitPattern.orNull,
        minorCommitPattern: String? = this.minorCommitPattern.orNull,
        patchCommitPattern: String? = this.patchCommitPattern.orNull,
    ): Any {
        if (override != null) {
            return override
        }

        val actualPrefix = "refs/tags/" + (tagPrefix ?: "")

        val repository = FileRepositoryBuilder().run {
            findGitDir(project.projectDir)
            gitDir ?: return project.version
            build()
        }

        val head = repository.resolve("HEAD") ?: return project.version
        val walk = RevWalk(repository).apply { markStart(parseCommit(head)) }

        val tagInfo = repository.refDatabase.getRefsByPrefix(actualPrefix).asSequence().mapNotNull { tag ->
            try {
                val version = Version.parseOrNull(tag.name.removePrefix(actualPrefix))
                val commit = walk.parseAny(tag.objectId).let {
                    when (it) {
                        is RevTag -> it.`object`.toObjectId()
                        is RevCommit -> it.toObjectId()
                        else -> null
                    }
                }
                if (version != null && commit != null) {
                    version to commit
                } else {
                    null
                }
            } catch (ex: Exception) {
                project.logger.debug("Failed to parse tag $tag", ex)
                null
            }
        }.lastOrNull()

        val tagVersion = tagInfo?.first ?: base?.let { Version.parseOrNull(it) } ?: Version()
        val tagCommit = tagInfo?.second

        val diff = IndexDiff(repository, "HEAD", FileTreeIterator(repository))

        val majorCommitRegex = majorCommitPattern?.toRegex()
        val minorCommitRegex = minorCommitPattern?.toRegex()
        val patchCommitRegex = patchCommitPattern?.toRegex()

        var versionMajor = tagVersion.major
        var versionMinor = tagVersion.minor
        var versionPatch = tagVersion.patch
        var versionQualifier = tagVersion.qualifier

        val commitMessages = arrayListOf<String>()
        while (true) {
            val current = walk.next() ?: break

            when {
                current.toObjectId() == tagCommit?.toObjectId() -> break
                else -> commitMessages.add(current.shortMessage ?: "")
            }
        }

        commitMessages.reversed().forEach {
            when {
                majorCommitRegex?.matches(it) == true -> {
                    versionMajor++
                    versionMinor = 0
                    versionPatch = 0
                }
                minorCommitRegex?.matches(it) == true -> {
                    versionMinor++
                    versionPatch = 0
                }
                patchCommitRegex?.matches(it) == true -> {
                    versionPatch++
                }
            }
        }


        if (qualifier == null && diff.diff()) {
            versionPatch++
            versionQualifier = "SNAPSHOT"
        } else {
            versionQualifier = qualifier ?: versionQualifier
        }

        return Version(versionMajor, versionMinor, versionPatch, versionQualifier).toString()
    }

    private data class Version(
        val major: Int = 0,
        val minor: Int = 0,
        val patch: Int = 0,
        val qualifier: String? = null,
    ) {
        companion object {
            fun parse(value: String): Version {
                val parts = value.substringBefore("-").split(".", limit = 3)
                val qualifier = value.substringAfter("-", missingDelimiterValue = "").takeUnless { it.isBlank() }

                return Version(
                    major = parts[0].toInt(),
                    minor = parts[1].toInt(),
                    patch = parts[2].toInt(),
                    qualifier = qualifier,
                )
            }

            fun parseOrNull(value: String?) = value?.let {
                try {
                    parse(it)
                } catch (ex: Exception) {
                    null
                }
            }
        }

        override fun toString(): String = "$major.$minor.$patch${qualifier?.let { "-$it" } ?: ""}"
    }
}