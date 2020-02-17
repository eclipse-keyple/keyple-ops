package org.eclipse.keyple.gradle

import java.net.URL

class KeypleVersioning {

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