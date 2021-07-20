package de.solugo.gradle.helm

import io.github.classgraph.ClassGraph
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.streams.toList

fun <T> withTemporaryFolder(block: File.() -> T) {
    val folder = Files.createTempDirectory("helm")
    try {
        block(folder.toFile())
    } finally {
        Files.walk(folder).toList().reversed().forEach {
            Files.delete(it)
        }
    }
}


fun File.git(block: Git.() -> Unit) {
    Git.init().apply {
        setDirectory(this@git)
        call().apply {
            resolve(".gitignore").writeText("/.gradle")
            block()
        }
    }
}

fun File.extract(path: String) {
    ClassGraph().acceptPaths(path).scan().allResources.forEach { resource ->
        resolve(resource.path.removePrefix(path).removePrefix("/")).also { target ->
            target.parentFile.mkdirs()
            target.outputStream().use { outputStream ->
                resource.open().use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        }
    }
}

fun File.gradle(vararg parameters: String, block: BuildResult.() -> Unit) {
    GradleRunner.create().run {
        withProjectDir(this@gradle)
        withArguments(*(arrayOf("--include-build", File(".").absolutePath) + parameters))
        withDebug(true)
        build().block()
    }
}