package de.solugo.gradle.gitversion

import de.solugo.gradle.test.core.Directory.Helper.extractDirectoryFromClasspath
import de.solugo.gradle.test.core.Directory.Helper.file
import de.solugo.gradle.test.core.Directory.Helper.withTemporaryDirectory
import de.solugo.gradle.test.core.Executor.Companion.execute
import de.solugo.gradle.test.core.GradleTest
import de.solugo.gradle.test.git.Git.Companion.git
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class GitVersionPluginTest {

    @Test
    fun `version is calculated successfully without git`() {
        GradleTest {
            withTemporaryDirectory {
                path.extractDirectoryFromClasspath("common", "simple")
            }
            execute("versionInfo") {
                assertThat(output).contains("Version: unspecified")
            }
        }
    }

    @Test
    fun `version is calculated successfully without tag`() {
        GradleTest {
            withTemporaryDirectory {
                path.extractDirectoryFromClasspath("common", "simple")
            }
            git {
                commit("Initial commit")
            }
            execute("versionInfo") {
                assertThat(output).contains("Version: 0.0.1")
            }
        }
    }

    @Test
    fun `version is calculated successfully for tag`() {
        GradleTest {
            withTemporaryDirectory {
                path.extractDirectoryFromClasspath("common", "simple")
            }
            git {
                commit("Initial commit")
                tag("1.0.0")
            }
            execute("versionInfo") {
                assertThat(output).contains("Version: 1.0.0")
            }
        }
    }

    @Test
    fun `version is calculated successfully for commit after tag`() {
        GradleTest {
            withTemporaryDirectory {
                path.extractDirectoryFromClasspath("common", "simple")
            }
            git {
                commit("Initial commit")
                tag("1.0.0")
                commit("Second commit")
            }
            execute("versionInfo") {
                assertThat(output).contains("Version: 1.0.1")
            }
        }
    }

    @Test
    fun `version is calculated successfully for commit on unparseable tag`() {
        GradleTest {
            withTemporaryDirectory {
                path.extractDirectoryFromClasspath("common", "simple")
            }
            git {
                commit("Initial commit")
                tag("1.0.0")
                commit("Second commit")
                tag("wrongformat")
            }
            execute("versionInfo") {
                assertThat(output).contains("Version: 1.0.1")
            }
        }
    }

    @Test
    fun `version is calculated successfully for commit on second tag`() {
        GradleTest {
            withTemporaryDirectory {
                path.extractDirectoryFromClasspath("common", "simple")
            }
            git {
                commit("Initial commit")
                tag("1.0.0")
                commit("Second commit")
                tag("2.0.0")
            }
            execute("versionInfo") {
                assertThat(output).contains("Version: 2.0.0")
            }
        }
    }

    @Test
    fun `version is calculated successfully for dirty state`() {
        GradleTest {
            withTemporaryDirectory {
                path.extractDirectoryFromClasspath("common", "simple")
            }
            git {
                commit("Initial commit")
                tag("1.0.0")
                commit("Second commit")
                tag("2.0.0")
                commit("Third commit")
            }
            file("content.txt") {
                writeText("changed")
            }
            execute("versionInfo") {
                assertThat(output).contains("Version: 2.0.2-SNAPSHOT")
            }
        }
    }

    @Test
    fun `version is calculated successfully with configured prefix`() {
        GradleTest {
            withTemporaryDirectory {
                path.extractDirectoryFromClasspath("common", "configured")
            }
            git {
                commit("Initial commit")
                tag("TEST-1.0.0")
                commit("Second commit")
                tag("2.0.0")
            }
            file("content.txt") {
                writeText("changed")
            }
            execute("versionInfo") {
                assertThat(output).contains("Version: 1.0.2")
            }
        }
    }

    @Test
    fun `version is calculated successfully with properties prefix`() {
        GradleTest {
            withTemporaryDirectory {
                path.extractDirectoryFromClasspath("common", "properties")
            }
            git {
                commit("Initial commit")
                tag("VERSION-1.0.0")
                commit("patch: Second commit")
                tag("2.0.0")
            }
            file("content.txt") {
                writeText("changed")
            }
            execute("versionInfo") {
                assertThat(output).contains("Version: 1.0.2")
            }
        }
    }

    @Test
    fun `version is calculated successfully with semantic history`() {
        GradleTest {
            withTemporaryDirectory {
                path.extractDirectoryFromClasspath("common", "properties")
            }
            git {
                commit("Initial commit")
                tag("VERSION-1.0.0")
                commit("breaking: breaking change")
                commit("feature: custom new feature")
                commit("try: tried to fix it")
                commit("patch: feature bugfix")
            }
            file("content.txt") {
                writeText("changed")
            }
            execute("versionInfo") {
                assertThat(output).contains("Version: 2.1.2-SNAPSHOT")
            }
        }
    }
}