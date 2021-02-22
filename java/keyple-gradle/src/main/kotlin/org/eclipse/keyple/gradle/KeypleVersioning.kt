package org.eclipse.keyple.gradle

import org.gradle.api.Project
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class KeypleVersioning {

    val snapshotRepo= "https://oss.sonatype.org/content/repositories/snapshots/"
    val releaseRepo = "https://oss.sonatype.org/content/repositories/releases/"
    val stagingRepo = "https://oss.sonatype.org/service/local/staging/deploy/maven2/"

    fun snapshotProject(project: Project) {
        val currentVersion = project.version.toString()
        project.version = if (currentVersion.endsWith("-SNAPSHOT")) currentVersion else "$currentVersion-SNAPSHOT"
    }

    fun hasNotAlreadyBeenReleased(project: Project): Boolean {
        val jarGroup = (project.group as String).replace('.', '/')
        val releasedVersion = project.version.toString().removeSuffix("-SNAPSHOT")
        val jarName = "${project.property("artifactId")}-$releasedVersion.jar"
        val repositoryPath = "$jarGroup/${project.property("artifactId")}/$releasedVersion/$jarName"

        val canBeUploaded = !urlExists(stagingRepo + repositoryPath)
                    && !urlExists(releaseRepo + repositoryPath)
        if (!canBeUploaded) {
            println("Artifacts already released, no need to upload it again.")
        }
        return canBeUploaded
    }

    fun urlExists(repositoryUrl: String): Boolean {
        return try {
            val connection = URL(repositoryUrl).openConnection() as HttpURLConnection
            connection.connectTimeout = 5_000
            connection.readTimeout = 1_000
            connection.requestMethod = "HEAD"
            connection.responseCode == 200
        } catch (ignored: IOException) {
            false
        }
    }

    fun getLastAlphaVersionFrom(baseVersion: String): String? {
        val mainVersion = baseVersion.replace(Regex("-.+"), "")
        val metadataUrl = "https://repo1.maven.org/maven2/org/eclipse/keyple/keyple-java-core/maven-metadata.xml"
        val metadataStream = URL(metadataUrl).openStream()
        val metadata = KeypleMarshaller().unmarshal(metadataStream, MavenGroupMetadata::class.java)
        val lastVersion= metadata
                .versioning
                .versions
                .filter { it.startsWith("$mainVersion-alpha") }
                .sorted()
                .lastOrNull()
        return lastVersion
    }

    fun getNextAlphaVersionFrom(baseVersion: String): String {
        val mainVersion = baseVersion.replace(Regex("-.+"), "")
        val lastVersion = getLastAlphaVersionFrom(mainVersion)
        val alphaVersion = lastVersion
                ?.replace(Regex(".*-alpha-"), "")
                ?.toInt()
                ?.run { this + 1 }
                ?: 1
        return "$mainVersion-alpha-$alphaVersion"
    }
}