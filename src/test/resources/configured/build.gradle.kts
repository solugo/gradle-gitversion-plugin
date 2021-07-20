plugins {
    id("de.solugo.gradle.gitversion")
}

gitVersion {
    prefix.set("TEST-")
    apply()
}

tasks.create("versionInfo").doFirst {
    println("Version: $version")
}
