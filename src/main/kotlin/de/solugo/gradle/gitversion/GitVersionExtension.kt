package de.solugo.gradle.gitversion

import org.eclipse.jgit.lib.IndexDiff
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.treewalk.FileTreeIterator
import org.gradle.api.Project
import org.gradle.util.VersionNumber

open class GitVersionExtension(
    private val project: Project
) {

    val versionNumber by lazy {
        val repository = FileRepositoryBuilder().run {
            findGitDir(project.projectDir)
            gitDir ?: return@lazy null
            build()
        }

        val prefix = "refs/tags/"
        val (tag, tagVersion) = repository.refDatabase.getRefsByPrefix(prefix).asSequence().mapNotNull { tag ->
            VersionNumber.parse(tag.name.removePrefix(prefix)).takeUnless {
                it == VersionNumber.UNKNOWN
            }?.let {
                tag to it
            }
        }.lastOrNull() ?: return@lazy null

        val diff = IndexDiff(repository, "HEAD", FileTreeIterator(repository))
        val walk = RevWalk(repository).apply { markStart(parseCommit(repository.resolve("HEAD"))) }
        val end = tag.let { walk.parseTag(it.objectId) }
        var count = 0
        while (true) {
            val commit = walk.next() ?: break
            if (commit.toObjectId() != end?.`object`?.toObjectId()) {
                count++
            } else {
                break
            }
        }

        val suffix = if (diff.diff()) {
            count++
            "SNAPSHOT"
        } else {
            null
        }

        VersionNumber(tagVersion.major, tagVersion.minor, tagVersion.micro + count, suffix)
    }

    val version; get() = versionNumber?.toString()

    fun apply() {
        version?.also { project.version = it }
    }

    override fun toString(): String = version ?: "unknown"
}