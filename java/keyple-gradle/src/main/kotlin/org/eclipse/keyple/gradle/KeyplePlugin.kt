package org.eclipse.keyple.gradle

import org.eclipse.keyple.gradle.pom.YamlToPom
import org.gradle.api.*
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.jvm.tasks.Jar
import org.gradle.plugins.signing.SigningExtension
import java.io.File
import kotlin.reflect.KProperty1

val Project.title: String
    get() = property("title") as String? ?: name

fun Project.prop(name: String): String? {
    return properties[name]?.toString()
}

@Suppress("UNCHECKED_CAST")
fun <R> read(instance: Any, propertyName: String): R {
    val property = instance::class.members
        .first { it.name == propertyName } as KProperty1<Any, *>
    return property.get(instance) as R
}

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
        versioning.init(project)
        println("Using Keyple Gradle ${javaClass.`package`.implementationVersion} for ${project.displayName}.")
        versioning.snapshotProject(project)
        setupTasks(project)
        setupLicense(project)

        project.tasks.getByName("build")
            .mustRunAfter("release")

        project.plugins.apply("maven-publish")
        project.tasks.findByName("javadoc")?.doFirst { javadoc ->
            javadoc as Javadoc
            val stylesheet = File(project.buildDir, "keyple-stylesheet.css")
            stylesheet.outputStream().use {
                javaClass.getResourceAsStream("javadoc/keyple-stylesheet.css")?.copyTo(it)
            }
            val javadocLogo = project.prop("javadoc.logo")
                ?: "<a target=\"_parent\" href=\"https://keyple.org/\"><img src=\"https://keyple.org/docs/api-reference/java-api/keyple-java-core/1.0.0/images/keyple.png\" height=\"20px\" style=\"background-color: white; padding: 3px; margin: 0 10px -7px 3px;\"/></a>"
            val javadocCopyright = project.prop("javadoc.copyright")
                ?: "Copyright &copy; Eclipse Foundation, Inc. All Rights Reserved."
            javadoc.options {
                it.overview = "src/main/javadoc/overview.html"
                it.windowTitle = project.title + " - " + project.version
                it.header("${javadocLogo}<span style=\"line-height: 30px\"> ${project.title} - ${project.version}</span>")
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
        project.tasks.findByName("jar")?.doFirst { jar ->
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

        project.afterEvaluate {
            project.extensions.configure(
                PublishingExtension::class.java,
                configurePublishing(project)
            )
        }
    }

    private fun setupTasks(project: Project) {
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
                outputs.upToDateWhen { versioning.isAlreadyReleased }
                doFirst {
                    project.version = project.version.toString().removeSuffix("-SNAPSHOT")
                    project.repositories
                        .removeIf {
                            it is MavenArtifactRepository
                                    && it.url.rawPath.contains("snapshot")
                        }
                    project.extensions.findByType(PublishingExtension::class.java)
                        ?.repositories
                        ?.first { it.name == "keypleRepo" }
                        ?.let { it as MavenArtifactRepository }
                        ?.apply { url = project.uri(versioning.stagingRepo) }
                }
                    .finalizedBy("publish")
            }

        project.task("setVersion")
            .doFirst(this::setVersion)

        project.task("getLastAlphaVersion")
            .doFirst(this::getLastAlphaVersion)

        project.task("setNextAlphaVersion")
            .doFirst(this::setNextAlphaVersion)
            .finalizedBy("setVersion")
    }

    private fun setupLicense(project: Project) {
        val licenseHeaderParent = project.rootDir
        licenseHeaderParent.mkdirs()
        val licenseHeader = File(licenseHeaderParent, "LICENSE_HEADER")
        if (!licenseHeader.isFile) {
            println("Creating licenseHeader in: $licenseHeader")
            project.logger.info("Creating licenseHeader in: $licenseHeader")
            licenseHeader.createNewFile()
            licenseHeader.setExecutable(false)
        }
        licenseHeader.outputStream().use {
            javaClass.getResourceAsStream("LICENSE_HEADER")?.copyTo(it)
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

    fun configurePublishing(project: Project): Action<PublishingExtension> {
        return Action { extension ->
            project.components.findByName("java")
                ?.let { java ->
                    extension.publications.create(
                        "mavenJava",
                        MavenPublication::class.java
                    ) { publication ->
                        publication.from(java)
                        project.prop("archivesBaseName")
                            ?.let { publication.artifactId = it }
                        val pomDetails = File(project.rootDir, "PUBLISHERS.yml")
                        if (pomDetails.exists()) {
                            YamlToPom(pomDetails.inputStream(), project)
                                .use { publication.pom(it::inject) }
                        }
                    }
                }
            project.components.findByName("release")
                ?.let { release ->
                    val releaseSourceJar =
                        project.extensions.findByName("android")
                            ?.let { read<NamedDomainObjectContainer<Any>>(it, "sourceSets") }
                            ?.findByName("main")
                            ?.let { read<Any>(it, "java") }
                            ?.let { read<Set<File>>(it, "srcDirs") }
                            ?.let { main ->
                                project.tasks.create("releaseSourcesJar", Jar::class.java) {
                                    it.archiveClassifier.set("sources")
                                    it.from(main)
                                }
                            }
                    val releaseDocJar =
                        project.tasks.findByName("dokkaHtml")?.let { dokkaJavadoc ->
                            project.tasks.create("releaseDocJar", Jar::class.java) {
                                it.dependsOn.add(dokkaJavadoc)
                                it.archiveClassifier.set("kdoc")
                                it.from(dokkaJavadoc)
                            }
                        }

                    extension.publications.create(
                        "mavenRelease",
                        MavenPublication::class.java
                    ) { publication ->
                        publication.from(release)
                        releaseSourceJar?.let { publication.artifact(it) }
                        releaseDocJar?.let { publication.artifact(it) }
                        project.prop("archivesBaseName")
                            ?.let { publication.artifactId = it }
                        val pomDetails = File(project.rootDir, "PUBLISHERS.yml")
                        if (pomDetails.exists()) {
                            YamlToPom(pomDetails.inputStream(), project)
                                .use { publication.pom(it::inject) }
                        }
                    }
                }
            extension.repositories.maven { maven ->
                maven.name = "keypleRepo"
                maven.credentials {
                    project.prop("ossrhUsername")?.let(it::setUsername)
                    project.prop("ossrhPassword")?.let(it::setPassword)
                }
                maven.url = project.uri(versioning.snapshotsRepo)
            }
            if (project.hasProperty("signing.keyId")) {
                project.plugins.apply("signing")
                project.extensions.configure(SigningExtension::class.java) { signing ->
                    extension.publications.findByName("mavenJava")
                        ?.let {
                            project.logger.info("Signing Java artifacts.")
                            signing.sign(it)
                        }
                    extension.publications.findByName("mavenRelease")
                        ?.let {
                            project.logger.info("Signing Android artifacts.")
                            signing.sign(it)
                        }
                }
            }
            if (project.hasProperty("signing.secretKeyRingFile")) {
                val secretFile = project.prop("signing.secretKeyRingFile")
                if (secretFile?.contains("~") == true) {
                    project.setProperty(
                        "signing.secretKeyRingFile",
                        secretFile.replace("~", System.getProperty("user.home"))
                    )
                }
            }
        }
    }
}
