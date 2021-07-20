plugins {
    id("de.solugo.gradle.gitversion")
}

version = gitVersion.version

tasks.create("versionInfo").doFirst {
    println("Version: $version")
}
