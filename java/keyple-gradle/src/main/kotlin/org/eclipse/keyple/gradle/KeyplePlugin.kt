package org.eclipse.keyple.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import java.util.*

class KeyplePlugin : Plugin<Project> {

    val versioning = KeypleVersioning()

    override fun apply(project: Project) {
        project.task("setVersion")
                .doFirst(this::setVersion)

        project.task("getLastAlphaVersion")
                .doFirst(this::getLastAlphaVersion)

        project.task("setNextAlphaVersion")
                .doFirst(this::setNextAlphaVersion)
                .finalizedBy("setVersion")
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
