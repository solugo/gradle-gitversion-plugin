[![License](https://img.shields.io/github/license/solugo/gradle-gitversion-plugin.svg?style=for-the-badge)](https://github.com/solugo/gradle-gitversion-plugin/blob/master/LICENSE)
[![Version](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/de/solugo/gradle/gradle-gitversion-plugin/maven-metadata.xml.svg?style=for-the-badge)](https://plugins.gradle.org/m2/de/solugo/gradle/gradle-gitversion-plugin/)

# [Gradle GitVersion Plugin](https://plugins.gradle.org/plugin/de.solugo.gradle.gitversion)

This plugin allows the calculation of the project version based on git tags and commits.

## Usage

```kotlin
plugins {
    id("de.solugo.gitversion") version "..."
}
```

## Tasks

- gitVersionPrintBuildInfo
: Prints the build information generated by gitVersion

- gitVersionCreatePropertiesFile
: Creates a file containing the generated version. 
By default the file ``build/VERSION`` is generated.

- gitVersionCreateVersionFile
: Creates a resource properties file containing the build information. 
By default the file ``build/resources/main/gitVersion.properties`` is generated.

## Gradle Properties

### gitVersionTrigger (default: ``git``)
The version value that triggers the calculation of a git based version.

### gitVersionBase (default: ``0.0.0``)
The base version used for the calculation of a git based version.

### gitVersionTagPattern (default: ``^(.+)$``)
The regex pattern used on tag name to identify a version tag.

### gitVersionMajorPattern
The regex pattern used on commit message to identify a major version bump.

### gitVersionMinorPattern
The regex pattern used on commit message to identify a minor version bump.

### gitVersionPatchPattern
The regex pattern used on commit message to identify a patch version bump.

### gitVersionQualifier (default: ``auto``)
Version qualifier to be appended to the calculated version

- ``auto``
: If there are uncommited changes ``SNAPSHOT`` is added as qualifier.

- ``hash``
: If there are uncommited changes ``SNAPSHOT`` or else the commit hash is added as qualifier.

- ``none``
: No qualifier will be added.

### ``gitVersionPipeline`` (default: ``auto``)
Build pipeline handling to be applied

### Pipeline Modification

- ``auto``
: Current build pipeline is identified automatically

- ``azure``
: Azure build pipeline modifications are applied if possible

- ``github``
: GitHub build pipeline modifications are applied if possible


## Calculation

### Default without tags

```text
VERSION                     2.1.1
COMMIT "Second commit"  <-- 0.0.2 (patch pattern)
COMMIT "First commit"   <-- 0.0.1 (patch pattern)
INIT                    <-- 0.0.0 (base)
```

### Default with tags

```text
VERSION                     2.1.1
COMMIT "Second commit"  <-- 1.0.1 (patch pattern ^(.*)$)
TAG "v1.0.0"            <-- 1.0.0 (tag pattern ^v(.+)$)
```

### Custom with tags

```text
VERSION                     2.1.1
COMMIT "Patch commit"   <-- 2.1.1 (patch pattern ^Patch (.*)$)
COMMIT "Minor commit"   <-- 2.1.0 (minor pattern ^Minor (.*)$)
COMMIT "Major commit"   <-- 2.0.0 (major pattern ^Major (.*)$)
TAG "Version-1.0.0"     <-- 1.0.0 (tag pattern ^Version-(.+)$)
```

#### Custom with tags and dirty tree

```text
VERSION                     2.1.2-SNAPSHOT
MODIFICATION            <-- 2.1.2-SNAPSHOT
COMMIT "Patch commit"   <-- 2.1.1 (patch pattern ^Patch (.*)$)
COMMIT "Minor commit"   <-- 2.1.0 (minor pattern ^Minor (.*)$)
COMMIT "Major commit"   <-- 2.0.0 (major pattern ^Major (.*)$)
TAG "Version-1.0.0"     <-- 1.0.0 (tag pattern ^Version-(.+)$)
```

#### Custom with tags and hash qualifier

```text
VERSION                     2.1.2-3af1a64
COMMIT "Patch commit"   <-- 2.1.1 (patch pattern ^Patch (.*)$)
COMMIT "Minor commit"   <-- 2.1.0 (minor pattern ^Minor (.*)$)
COMMIT "Major commit"   <-- 2.0.0 (major pattern ^Major (.*)$)
TAG "Version-1.0.0"     <-- 1.0.0 (tag pattern ^Version-(.+)$)
```