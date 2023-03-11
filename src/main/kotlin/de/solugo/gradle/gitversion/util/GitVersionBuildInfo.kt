package de.solugo.gradle.gitversion.util

import java.io.Serializable

data class GitVersionBuildInfo(
    val version: String,
    val timestamp: String,
    val gitHash: String?,
    val gitTimestamp: String?,
) : Serializable