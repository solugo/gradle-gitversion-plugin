plugins {
    id("org.jetbrains.kotlin.jvm") version "1.5.20"
    id("com.gradle.plugin-publish") version "0.10.0"
    id("maven-publish")
    id("java-gradle-plugin")
}

group = "de.solugo.gradle"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(gradleApi())
    implementation("org.eclipse.jgit:org.eclipse.jgit:5.12.0.202106070339-r")

    testImplementation(gradleTestKit())
    testImplementation("org.assertj:assertj-core:3.20.2")
    testImplementation("io.github.classgraph:classgraph:4.8.110")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.3.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.3.1")
}

gradlePlugin {
    plugins {
        create("gitversionPlugin") {
            id = "de.solugo.gradle.gitversion"
            implementationClass = "de.solugo.gradle.gitversion.GitVersionPlugin"
        }
    }
}

pluginBundle {
    website = "https://github.com/solugo/gradle-gitversion-plugin"
    vcsUrl = "https://github.com/solugo/gradle-gitversion-plugin.git"

    plugins {
        getByName("gitversionPlugin") {
            displayName = "Gradle Git Version plugin"
            description = "Plugin for version calculation based on git commits"
            tags = listOf("git", "version")
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
}