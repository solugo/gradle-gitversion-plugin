plugins {
    id("java-gradle-plugin")
    id("maven-publish")
    id("org.jetbrains.kotlin.jvm") version "1.8.10"
    id("com.gradle.plugin-publish") version "1.1.0"
}

group = "de.solugo.gradle"

repositories {
    mavenCentral()
}

dependencies {
    val gradleTestVersion = "1.0.3"
    val junitVersion = "5.8.1"

    implementation(gradleApi())
    implementation("org.eclipse.jgit:org.eclipse.jgit:6.5.0.202303070854-r")
    implementation("org.semver:api:0.9.33")

    testImplementation(gradleTestKit())
    testImplementation("de.solugo.gradle.test:gradle-test-core:$gradleTestVersion")
    testImplementation("de.solugo.gradle.test:gradle-test-git:$gradleTestVersion")
    testImplementation("org.assertj:assertj-core:3.21.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

gradlePlugin {
    plugins {
        create("gitVersionPlugin") {
            id = "de.solugo.gitversion"
            implementationClass = "de.solugo.gradle.gitversion.GitVersionPlugin"
            displayName = "Gradle Git Version plugin"
            description = "Plugin for version calculation based on git commits"
        }
    }
}

kotlin {
    jvmToolchain(11)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
