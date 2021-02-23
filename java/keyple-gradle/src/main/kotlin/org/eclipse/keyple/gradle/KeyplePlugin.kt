package org.eclipse.keyple.gradle

import org.eclipse.keyple.gradle.pom.YamlToPom
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.jvm.tasks.Jar
import org.gradle.plugins.signing.SigningExtension
import java.io.File
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
                val pomDetails = File(project.projectDir, "PUBLISHERS.yml")
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
                    maven.url =
                        project.uri("https://oss.sonatype.org/content/repositories/snapshots/")
                } else {
                    maven.url =
                        project.uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
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
        project.tasks.getByName("javadoc")
            .doFirst { javadoc ->
                val stylesheet = File(project.buildDir, "keyple-stylesheet.css")
                stylesheet.outputStream().use {
                    javaClass.getResourceAsStream("javadoc/keyple-stylesheet.css")?.copyTo(it)
                }
                (javadoc as Javadoc).options {
                    it.encoding = "UTF-8"
                    it.overview = "src/main/javadoc/overview.html"
                    it.windowTitle = project.name + " - " + project.version
                    it.header(
                        "<a target=\"_parent\" href=\"https://keyple.org/\">" +
                                "<img src=\"https://keyple.org/docs/api-reference/java-api/keyple-java-core/1.0.0/images/keyple.png\" height=\"20px\" style=\"background-color: white; padding: 3px; margin: 0 10px -7px 3px;\"/>" +
                                "</a><span style=\"line-height: 30px\"> " + project.name + " - " + project.version + "</span>"
                    )
                        .docTitle(project.name + " - " + project.version)
                        .stylesheetFile(stylesheet)
                        .footer("Copyright &copy; Eclipse Foundation, Inc. All Rights Reserved.")
                }
            }
        project.tasks.getByName("jar")
            .doFirst { jar ->
                copy(File(project.projectDir, "LICENCE"), File(project.buildDir, "/resources/main/META-INF/"))
                copy(File(project.projectDir, "NOTICE.md"), File(project.buildDir, "/resources/main/META-INF/"))
                (jar as Jar).manifest { manifest ->
                    manifest.attributes(
                        mapOf(
                            "Implementation-Title" to project.name,
                            "Implementation-Version" to project.version
                        )
                    )
                }
            }
    }

    private fun copy(source: File, target: File) {
        if (!source.isFile) return;
        if (target.isDirectory) {
            File(target, source.name)
                .outputStream()
                .use { source.inputStream().copyTo(it) }
        } else {
            target.outputStream()
                .use { source.inputStream().copyTo(it) }
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

        println("Setting new version for ${task.project.name} to ${task.project.version}")
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
