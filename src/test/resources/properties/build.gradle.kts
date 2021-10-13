plugins {
    id("de.solugo.gitversion")
}

version = gitVersion.version()

tasks.create("versionInfo").doFirst {
    println("Version: $version")
}
