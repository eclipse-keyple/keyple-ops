package org.eclipse.keyple.gradle

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.net.URL

internal class KeypleMarshallerTest {

    @Test
    fun unmarshal() {
        val metadataStream = URL("https://repo1.maven.org/maven2/org/eclipse/keyple/keyple-java-core/maven-metadata.xml")
                .openStream()
        val metadata = KeypleMarshaller().unmarshal(metadataStream, MavenGroupMetadata::class.java)
        assertThat(metadata.artifactId).isEqualTo("keyple-java-core")
        assertThat(metadata.versioning.versions).isNotEmpty()
    }
}