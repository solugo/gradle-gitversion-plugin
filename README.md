[![License](https://img.shields.io/github/license/solugo/gradle-gitversion-plugin.svg?style=for-the-badge)](https://github.com/solugo/gradle-gitversion-plugin/blob/master/LICENSE)
[![Version](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/de/solugo/gradle/gradle-gitversion-plugin/maven-metadata.xml.svg?style=for-the-badge)](https://plugins.gradle.org/m2/de/solugo/gradle/gradle-gitversion-plugin/)

# [Gradle GitVersion Plugin](https://plugins.gradle.org/plugin/de.solugo.gradle.gitversion)

This plugin allows the calculation of the project version based on git tags and commits.

## Usage

```kotlin
plugins {
    id("de.solugo.gradle.gitversion") version "..."
}

group = "some.group"
version = gitVersion.version(
    override = "...", //optional
    base = "...", //optional
    qualifier = "...", //optional
    tagPrefix = "...", //optional
    majorCommitPattern = "...", //optional
    minorCommitPattern = "...", //optional
    patchCommitPattern = "...", //optional
)
```

### Configuration

```kotlin
gitVersion {
    override.set("...") // version override (default: null)
    base.set("...") // base version (default: "0.0.0")
    qualifier.set("...") // base version (default: null)
    tagPrefix.set("...") // version tag prefix (default: null)
    majorCommitPattern.set("...") // major commit pattern (default: null) 
    minorCommitPattern.set("...") // minor commit pattern (default: null) 
    patchCommitPattern.set("...") // patch commit pattern (default: ".*") 
}
```

### Properties

```properties
gitVersionOverride=...
gitVersionBase=...
gitVersionQualifier=...
gitVersionTagPrefix=...
gitVersionMajorCommitPattern=...
gitVersionMinorCommitPattern=...
gitVersionPatchCommitPattern=...
```