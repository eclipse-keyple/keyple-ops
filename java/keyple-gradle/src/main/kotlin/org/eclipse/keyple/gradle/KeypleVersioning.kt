package org.eclipse.keyple.gradle

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import org.gradle.api.Project

class KeypleVersioning {

  var releasesRepo = "https://repo.maven.apache.org/maven2/"
  var isAlreadyReleased = false
  val stagingEndpoint
    get() = "https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/"

  val snapshotsEndpoint
    get() = "https://central.sonatype.com/repository/maven-snapshots/"

  fun init(project: Project) {
    project.prop("sonatype.url")?.let { releasesRepo = it }
    checkIfAlreadyReleased(project)
  }

  fun snapshotProject(project: Project) {
    val currentVersion = project.version.toString()
    project.version =
        if (currentVersion.endsWith("-SNAPSHOT")) currentVersion else "$currentVersion-SNAPSHOT"
  }

  private fun checkIfAlreadyReleased(project: Project) {
    val jarGroup = (project.group as String).replace('.', '/')
    val releasedVersion = project.version.toString().removeSuffix("-SNAPSHOT")
    val pomName = "${project.name}-$releasedVersion.pom"
    val repositoryPath = "$jarGroup/${project.name}/$releasedVersion/$pomName"

    isAlreadyReleased = urlExists(releasesRepo + repositoryPath)
    if (isAlreadyReleased) {
      println("Artifacts already released, no need to upload it again.")
    }
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
}
