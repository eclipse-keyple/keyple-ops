package org.eclipse.keyple.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import java.util.*

class KeyplePlugin : Plugin<Project> {

    val versioning = KeypleVersioning()

    override fun apply(project: Project) {
        project.task("setVersion")
                .doFirst() {
                    val gradleProperties = Properties()
                    val gradleFile = project.file("gradle.properties")
                    if (gradleFile.exists()) {
                        gradleProperties.load(gradleFile.inputStream())
                    }
                    gradleProperties.setProperty("version", "${project.version}")

                    gradleFile.writer().use {
                        gradleProperties.store(it)
                    }

                    println("Setting new version for ${project.description} to ${project.version}")
                }

        project.task("getLastAlphaVersion")
                .doFirst() {
                    val lastVersion = versioning
                            .getLastAlphaVersionFrom(project.version as String)
                    println("Looking for alpha in ${project.version}, found: $lastVersion")
                }

        project.task("setNextAlphaVersion")
                .doFirst() {
                    val nextVersion = versioning
                            .getNextAlphaVersionFrom(project.version as String)
                    project.version = nextVersion
                }
                .finalizedBy("setVersion")
    }
}
