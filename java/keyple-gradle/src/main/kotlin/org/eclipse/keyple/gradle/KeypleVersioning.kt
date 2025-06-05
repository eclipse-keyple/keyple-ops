package org.eclipse.keyple.gradle

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import org.gradle.api.Project

class KeypleVersioning {

  var repoServer = "https://ossrh-staging-api.central.sonatype.com"
  var isAlreadyReleased = false
  val snapshotsRepo
    get() = "${repoServer}/content/repositories/snapshots/"

  val releasesRepo
    get() = "${repoServer}/content/repositories/releases/"

  val stagingRepo
    get() = "${repoServer}/service/local/staging/deploy/maven2/"

  fun init(project: Project) {
    project.prop("sonatype.url")?.let { repoServer = it }
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
