package org.eclipse.keyple.gradle

import org.eclipse.keyple.gradle.pom.YamlToPom
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.jvm.tasks.Jar
import org.gradle.plugins.signing.SigningExtension
import java.io.File

val Project.title: String
    get() = property("title") as String? ?: name

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
        val property = { name: String ->
            project.properties[name]?.toString()
        }

        val licenseHeaderParent = File(project.projectDir, "gradle/")
        licenseHeaderParent.mkdirs()
        val licenseHeader = File(licenseHeaderParent,"license_header.txt")
        if (!licenseHeader.isFile) {
            licenseHeader.createNewFile()
        }
        licenseHeader.outputStream().use {
            javaClass.getResourceAsStream("license_header.txt")?.copyTo(it)
        }

        project.task("install")
            .apply {
                group = "publishing"
                description =
                    "Publishes all Maven publications produced by this project to the local Maven cache."
            }
            .dependsOn("publishToMavenLocal")

        project.task("release")
            .apply {
                group = "publishing"
                description =
                    "Releases all Maven publications produced by this project to Maven Central."
                if (!versioning.hasNotAlreadyBeenReleased(project)) {
                    doFirst {
                        project.version = project.version.toString().removeSuffix("-SNAPSHOT")
                        project.repositories
                            .removeIf {
                                it is MavenArtifactRepository
                                        && it.url.rawPath.contains("snapshot")
                            }
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
                publication.from(project.components.findByName("java"))
                val pomDetails = File(project.projectDir, "PUBLISHERS.yml")
                if (pomDetails.exists()) {
                    YamlToPom(pomDetails.inputStream(), project)
                        .use { publication.pom(it::inject) }
                }
            }
            extension.repositories.maven { maven ->
                maven.credentials {
                    property("ossrhUsername")?.let(it::setUsername)
                    property("ossrhPassword")?.let(it::setPassword)
                }
                val sonatypeUrl = property("sonatype.url") ?: "https://oss.sonatype.org"
                if (project.version.toString().endsWith("-SNAPSHOT")) {
                    maven.url =
                        project.uri("${sonatypeUrl}/content/repositories/snapshots/")
                } else {
                    maven.url =
                        project.uri("${sonatypeUrl}/service/local/staging/deploy/maven2/")
                }
            }
            if (project.hasProperty("signing.keyId")) {
                project.plugins.apply("signing")
                project.extensions.configure(SigningExtension::class.java) { signing ->
                    println("Signing artifacts.")
                    signing.sign(extension.publications.findByName("mavenJava"))
                }
            }
            if (project.hasProperty("signing.secretKeyRingFile")) {
                val secretFile = property("signing.secretKeyRingFile")
                if (secretFile?.contains("~") == true) {
                    project.setProperty("signing.secretKeyRingFile",
                        secretFile.replace("~", System.getProperty("user.home")))
                }
            }
        }
        project.tasks.findByName("javadoc")
            ?.doFirst { javadoc ->
                val stylesheet = File(project.buildDir, "keyple-stylesheet.css")
                stylesheet.outputStream().use {
                    javaClass.getResourceAsStream("javadoc/keyple-stylesheet.css")?.copyTo(it)
                }
                val javadocLogo = property("javadoc.logo")
                    ?: "<a target=\"_parent\" href=\"https://keyple.org/\"><img src=\"https://keyple.org/docs/api-reference/java-api/keyple-java-core/1.0.0/images/keyple.png\" height=\"20px\" style=\"background-color: white; padding: 3px; margin: 0 10px -7px 3px;\"/></a>"
                val javadocCopyright = property("javadoc.copyright")
                    ?: "Copyright &copy; Eclipse Foundation, Inc. All Rights Reserved."
                (javadoc as Javadoc).options {
                    it.overview = "src/main/javadoc/overview.html"
                    it.windowTitle = project.title + " - " + project.version
                    it.header(javadocLogo +
                                "<span style=\"line-height: 30px\"> " + project.title + " - " + project.version + "</span>")
                        .docTitle(project.title + " - " + project.version)
                        .use(true)
                        .stylesheetFile(stylesheet)
                        .footer(javadocCopyright)
                        .apply {
                            if ((System.getProperty("java.version")
                                    ?.split('.', limit = 2)
                                    ?.get(0)?.toInt() ?: 0) >= 11
                            ) {
                                addBooleanOption("-no-module-directories", true)
                            }
                        }
                }
            }
        project.tasks.findByName("jar")
            ?.doFirst { jar ->
                copy(
                    File(project.projectDir, "LICENSE"),
                    File(project.buildDir, "/resources/main/META-INF/")
                )
                copy(
                    File(project.projectDir, "NOTICE.md"),
                    File(project.buildDir, "/resources/main/META-INF/")
                )
                (jar as Jar).manifest { manifest ->
                    manifest.attributes(
                        mapOf(
                            "Implementation-Title" to project.title,
                            "Implementation-Version" to project.version
                        )
                    )
                }
            }
    }

    private fun copy(source: File, target: File) {
        if (!source.isFile) return;
        if (!target.isDirectory) {
            target.mkdirs()
        }
        File(target, source.name)
            .outputStream()
            .use { source.inputStream().copyTo(it) }
    }

    /**
     * Sets version inside the gradle.properties file
     * Usage: ./gradlew setVersion -P version=1.0.0
     */
    fun setVersion(task: Task) {
        val backupFile = task.project.file("gradle.properties.bak")
        backupFile.delete()
        val propsFile = task.project.file("gradle.properties")
        propsFile.renameTo(backupFile)

        var version = task.project.version as String
        version = version.removeSuffix("-SNAPSHOT")
        propsFile.printWriter().use {
            var versionApplied = false
            backupFile.readLines()
                .forEach { line ->
                    if (line.matches(Regex("version\\s*=.*"))) {
                        versionApplied = true
                        it.println("version = $version")
                    } else {
                        it.println(line)
                    }
                }
            if (!versionApplied) {
                it.println("version = $version")
            }
        }

        println("Setting new version for ${task.project.name} to $version")
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
