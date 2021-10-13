plugins {
    id("de.solugo.gitversion")
}

gitVersion {
    tagPrefix.set("TEST-")
}

version = gitVersion.version()

tasks.create("versionInfo").doFirst {
    println("Version: $version")
}
