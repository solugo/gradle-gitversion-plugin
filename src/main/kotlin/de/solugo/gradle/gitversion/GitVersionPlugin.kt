package de.solugo.gradle.gitversion

import org.gradle.api.Plugin
import org.gradle.api.Project

class GitVersionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.create("gitVersion", GitVersionExtension::class.java, project)
    }
}