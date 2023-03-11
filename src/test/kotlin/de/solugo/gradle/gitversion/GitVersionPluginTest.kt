package de.solugo.gradle.gitversion

import de.solugo.gradle.test.core.Directory.Helper.extractDirectoryFromClasspath
import de.solugo.gradle.test.core.Directory.Helper.extractFileFromClasspath
import de.solugo.gradle.test.core.Directory.Helper.file
import de.solugo.gradle.test.core.Directory.Helper.withTemporaryDirectory
import de.solugo.gradle.test.core.Executor.Companion.execute
import de.solugo.gradle.test.core.GradleTest
import de.solugo.gradle.test.git.Git.Companion.git
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class GitVersionPluginTest {

    @Test
    fun `shows correct task descriptions`() {
        GradleTest {
            withTemporaryDirectory {
                path.extractDirectoryFromClasspath("common")
                path.extractFileFromClasspath("other/gitignore", ".gitignore")
            }
            execute("tasks") {
                assertThat(output).contains(
                    """
                    GitVersion tasks
                    ----------------
                    gitVersionCreatePropertiesFile - Create gitVersion properties file
                    gitVersionCreateVersionFile - Create gitVersion version file
                    gitVersionPrintBuildInfo - Print gitVersion build info
                    """.trimIndent()
                )
            }
        }
    }

    @Test
    fun `version is not calculated for missing git repository`() {
        GradleTest {
            withTemporaryDirectory {
                path.extractDirectoryFromClasspath("common")
                path.extractFileFromClasspath("other/gitignore", ".gitignore")
            }
            execute("-PgitVersionPipeline=none", "-Pversion=git", "gitVersionPrintBuildInfo") {
                assertThat(output).contains("Could not calculate git version")
                assertThat(output).contains("Could not find git repository")
            }
        }
    }

    @Test
    fun `version is not calculated if current version is not 'gitversion'`() {
        GradleTest {
            withTemporaryDirectory {
                path.extractDirectoryFromClasspath("common")
                path.extractFileFromClasspath("other/gitignore", ".gitignore")
            }
            git {
                commit("Initial commit")
                tag("v1.0.0")
                commit("Second commit")
            }
            execute("-PgitVersionPipeline=none", "-Pversion=custom", "gitVersionPrintBuildInfo") {
                assertThat(output).contains("Version: custom")
            }
        }
    }

    @Test
    fun `version is calculated using defaults`() {
        GradleTest {
            withTemporaryDirectory {
                path.extractDirectoryFromClasspath("common")
                path.extractFileFromClasspath("other/gitignore", ".gitignore")
            }
            git {
                commit("Initial commit")
                tag("v1.0.0")
                commit("Second commit")
            }
            execute("-PgitVersionPipeline=none", "-Pversion=git", "gitVersionPrintBuildInfo") {
                assertThat(output).contains("Version: 1.0.1")
            }
        }
    }

    @Test
    fun `version is calculated using custom patterns`() {
        GradleTest {
            withTemporaryDirectory {
                path.extractDirectoryFromClasspath("common")
                path.extractFileFromClasspath("other/gitignore", ".gitignore")
                path.extractFileFromClasspath("properties/custom.properties", "gradle.properties")
            }
            git {
                commit("Initial commit")
                tag("Version-1.0.0")
                commit("Major change")
                commit("Minor change")
                commit("Patch change")
            }
            execute("-PgitVersionPipeline=none", "-Pversion=git", "gitVersionPrintBuildInfo") {
                assertThat(output).contains("Version: 2.1.1")
            }
        }
    }

    @Test
    fun `qualifier default qualifier clean state is set correctly`() {
        GradleTest {
            withTemporaryDirectory {
                path.extractDirectoryFromClasspath("common")
                path.extractFileFromClasspath("other/gitignore", ".gitignore")
            }
            git {
                commit("Initial commit")
                tag("v1.0.0")
                commit("Second commit")
            }
            execute("-PgitVersionPipeline=none", "-Pversion=git", "gitVersionPrintBuildInfo") {
                assertThat(output).contains("Version: 1.0.1")
            }
        }
    }

    @Test
    fun `qualifier 'auto' for dirty state is set correctly`() {
        GradleTest {
            withTemporaryDirectory {
                path.extractDirectoryFromClasspath("common")
                path.extractFileFromClasspath("other/gitignore", ".gitignore")
            }
            git {
                commit("Initial commit")
                tag("v1.0.0")
                commit("Second commit")
                file("test").writeText("Hallo")
            }
            execute("-PgitVersionPipeline=none", "-Pversion=git", "gitVersionPrintBuildInfo") {
                assertThat(output).contains("Version: 1.0.2-SNAPSHOT")
            }
        }
    }

    @Test
    fun `qualifier for 'hash' for clean state is set correctly`() {
        GradleTest {
            withTemporaryDirectory {
                path.extractDirectoryFromClasspath("common")
                path.extractFileFromClasspath("other/gitignore", ".gitignore")
            }
            git {
                commit("Initial commit")
                tag("v1.0.0")
                commit("Second commit")
            }
            execute(
                "-Pversion=git",
                "-PgitVersionQualifier=hash",
                "-PgitVersionPipeline=none",
                "gitVersionPrintBuildInfo",
            ) {
                assertThat(output).contains("Version: 1.0.1-")
            }
        }
    }

    @Test
    fun `'hash' qualifier dirty state is set correctly`() {
        GradleTest {
            withTemporaryDirectory {
                path.extractDirectoryFromClasspath("common")
                path.extractFileFromClasspath("other/gitignore", ".gitignore")
            }
            git {
                commit("Initial commit")
                tag("v1.0.0")
                commit("Second commit")
                file("test").writeText("Hallo")
            }
            execute(
                "-Pversion=git",
                "-PgitVersionPipeline=none",
                "-PgitVersionQualifier=hash",
                "gitVersionPrintBuildInfo",
            ) {
                assertThat(output).contains("Version: 1.0.2-SNAPSHOT")
            }
        }
    }

    @Test
    fun `properties file is generated`() {
        GradleTest {
            withTemporaryDirectory {
                path.extractDirectoryFromClasspath("common")
                path.extractFileFromClasspath("other/gitignore", ".gitignore")
            }
            git {
                commit("Initial commit")
                tag("v1.0.0")
                commit("Second commit")
            }
            execute("-PgitVersionPipeline=none", "-Pversion=git", "gitVersionCreatePropertiesFile") {
                val file = file("build/resources/main/gitVersion.properties")
                val content = file.readText()

                assertThat(content).contains("version=1.0.1")
            }
        }
    }

    @Test
    fun `version file is generated`() {
        GradleTest {
            withTemporaryDirectory {
                path.extractDirectoryFromClasspath("common")
                path.extractFileFromClasspath("other/gitignore", ".gitignore")
            }
            git {
                commit("Initial commit")
                tag("v1.0.0")
                commit("Second commit")
            }
            execute("-PgitVersionPipeline=none", "-Pversion=git", "gitVersionCreateVersionFile") {
                val file = file("build/VERSION")
                val content = file.readText()

                assertThat(content).contains("1.0.1")
            }
        }
    }

}