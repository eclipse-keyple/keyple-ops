package org.eclipse.keyple.gradle

import org.eclipse.keyple.gradle.pom.YamlToPom
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.maven.MavenDeployment
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.plugins.signing.SigningExtension
import java.io.File
import java.lang.IllegalStateException
import java.net.URI
import java.util.*

/**
 * Define multiple tasks:
 * Use `./gradlew build` to build project
 * Use `./gradlew install` to deploys to local maven repo
 * Use `./gradlew publish` to publish snapshot to maven central
 * Use `./gradlew release` to publish release to maven central
 */
class KeyplePlugin : Plugin<Project> {

    val versioning = KeypleVersioning()

    override fun apply(project: Project) {
        versioning.snapshotProject(project)

        project.task("install")
            .dependsOn("publishToMavenLocal")

        project.task("release")
            .also {
                if (!versioning.hasNotAlreadyBeenReleased(project)) {
                    it.doFirst {
                        project.version = project.version.toString().removeSuffix("-SNAPSHOT")
                    }.finalizedBy("build", "test", "publish")
                }
            }

        project.task("setVersion")
            .doFirst(this::setVersion)

        project.task("getLastAlphaVersion")
            .doFirst(this::getLastAlphaVersion)

        project.task("setNextAlphaVersion")
            .doFirst(this::setNextAlphaVersion)
            .finalizedBy("setVersion")

        project.plugins.apply("maven-publish")
        project.extensions.configure(PublishingExtension::class.java) { extension ->
            extension.publications.create(
                "mavenJava",
                MavenPublication::class.java
            ) { publication ->
                publication.from(project.components.getByName("java"))
                val pomDetails = File(project.projectDir, "PUBLISH.yml")
                if (pomDetails.exists()) {
                    YamlToPom(pomDetails.inputStream())
                        .use { publication.pom(it::inject) }
                }
            }
            extension.repositories.maven { maven ->
                maven.credentials {
                    it.username = System.getenv("ossrhUsername") ?: ""
                    it.password = System.getenv("ossrhPassword") ?: ""
                }
                if (project.version.toString().endsWith("-SNAPSHOT")) {
                    maven.url = URI.create("https://oss.sonatype.org/content/repositories/snapshots/")
                } else {
                    maven.url = URI.create("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
                }
            }
            if (project.hasProperty("signing.keyId")) {
                project.plugins.apply("signing")
                project.extensions.configure(SigningExtension::class.java) { signing ->
                    println("Signing artifacts.")
                    signing.sign(extension.publications.getByName("mavenJava"))
                }
            }
        }
    }

    /**
     * Sets version inside the gradle.properties file
     * Usage: ./gradlew setVersion -Pversion=1.0.0
     */
    fun setVersion(task: Task) {
        val gradleProperties = Properties()
        val gradleFile = task.project.file("gradle.properties")
        if (gradleFile.exists()) {
            gradleProperties.load(gradleFile.inputStream())
        }
        gradleProperties.setProperty("version", "${task.project.version}")

        gradleFile.writer().use {
            gradleProperties.store(it)
        }

        println("Setting new version for ${task.project.description} to ${task.project.version}")
    }

    /**
     * Prints to console the last alpha released version found on Maven Central
     * Usage: ./gradlew getLastAlphaVersion
     */
    fun getLastAlphaVersion(task: Task) {
        val lastVersion = versioning
            .getLastAlphaVersionFrom(task.project.version as String)
        println("Looking for alpha in ${task.project.version}, found: $lastVersion")
    }

    /**
     * Sets the next alpha version to be released based on last one found on Maven Central
     * Usage: ./gradlew setNextAlphaVersion
     */
    fun setNextAlphaVersion(task: Task) {
        val nextVersion = versioning
            .getNextAlphaVersionFrom(task.project.version as String)
        task.project.version = nextVersion
    }

}
