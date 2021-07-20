package de.solugo.gradle.gitversion

import de.solugo.gradle.helm.extract
import de.solugo.gradle.helm.git
import de.solugo.gradle.helm.gradle
import de.solugo.gradle.helm.withTemporaryFolder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


class GitVersionPluginTest {

    @Test
    fun `version is calculated successfully without git`() {
        withTemporaryFolder {
            extract("simple")
            gradle("versionInfo") {
                assertThat(output).contains("Version: unspecified")
            }
        }
    }

    @Test
    fun `version is calculated successfully for tag`() {
        withTemporaryFolder {
            extract("simple")
            git {
                add().apply {
                    addFilepattern(".")
                    call()
                }
                commit().apply {
                    setAll(true)
                    message = "Initial commit"
                    call()
                }
                tag().apply {
                    name = "1.0.0"
                    call()
                }
            }
            gradle("versionInfo") {
                assertThat(output).contains("Version: 1.0.0")
            }
        }
    }

    @Test
    fun `version is calculated successfully for commit after tag`() {
        withTemporaryFolder {
            extract("simple")
            git {
                add().apply {
                    addFilepattern(".")
                    call()
                }
                commit().apply {
                    setAll(true)
                    message = "Initial commit"
                    call()
                }
                tag().apply {
                    name = "1.0.0"
                    call()
                }
                commit().apply {
                    setAllowEmpty(true)
                    message = "Second commit"
                    call()
                }
            }
            gradle("versionInfo") {
                assertThat(output).contains("Version: 1.0.1")
            }
        }
    }

    @Test
    fun `version is calculated successfully for commit on unparseable tag`() {
        withTemporaryFolder {
            extract("simple")
            git {
                add().apply {
                    addFilepattern(".")
                    call()
                }
                commit().apply {
                    message = "Initial commit"
                    call()
                }
                tag().apply {
                    name = "1.0.0"
                    call()
                }
                commit().apply {
                    setAllowEmpty(true)
                    message = "Second commit"
                    call()
                }
                tag().apply {
                    name = "wrongformat"
                    call()
                }
            }
            gradle("versionInfo") {
                assertThat(output).contains("Version: 1.0.1")
            }
        }
    }

    @Test
    fun `version is calculated successfully for commit on second tag`() {
        withTemporaryFolder {
            extract("simple")
            git {
                add().apply {
                    addFilepattern(".")
                    call()
                }
                commit().apply {
                    message = "Initial commit"
                    call()
                }
                tag().apply {
                    name = "1.0.0"
                    call()
                }
                commit().apply {
                    setAllowEmpty(true)
                    message = "Second commit"
                    call()
                }
                tag().apply {
                    name = "2.0.0"
                    call()
                }
            }
            gradle("versionInfo") {
                assertThat(output).contains("Version: 2.0.0")
            }
        }
    }

    @Test
    fun `version is calculated successfully for dirty state`() {
        withTemporaryFolder {
            extract("simple")
            git {
                commit().apply {
                    message = "Initial commit"
                    call()
                }
                tag().apply {
                    name = "1.0.0"
                    call()
                }
                commit().apply {
                    setAllowEmpty(true)
                    message = "Second commit"
                    call()
                }
                tag().apply {
                    name = "2.0.0"
                    call()
                }
                commit().apply {
                    setAllowEmpty(true)
                    message = "Third commit"
                    call()
                }
            }
            resolve("content.txt").writeText("changed")
            gradle("versionInfo") {
                assertThat(output).contains("Version: 2.0.2-SNAPSHOT")
            }
        }
    }

    @Test
    fun `version is calculated successfully with prefix`() {
        withTemporaryFolder {
            extract("configured")
            git {
                commit().apply {
                    message = "Initial commit"
                    call()
                }
                tag().apply {
                    name = "TEST-1.0.0"
                    call()
                }
                commit().apply {
                    setAllowEmpty(true)
                    message = "Second commit"
                    call()
                }
                tag().apply {
                    name = "2.0.0"
                    call()
                }
            }
            resolve("content.txt").writeText("changed")
            gradle("versionInfo") {
                assertThat(output).contains("Version: 1.0.2")
            }
        }
    }
}