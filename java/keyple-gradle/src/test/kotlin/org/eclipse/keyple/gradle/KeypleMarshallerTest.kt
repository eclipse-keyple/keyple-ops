package org.eclipse.keyple.gradle

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.net.URL
import java.nio.charset.StandardCharsets

internal class KeypleMarshallerTest {

    @Test
    fun marshal() {
        val metadata = MavenGroupMetadata()
        metadata.groupId = "test-groupId"
        metadata.artifactId = "test-artifactId"
        metadata.versioning.latest = "1.0.0-LATEST"
        metadata.versioning.release = "1.0.0-RELEASE"
        metadata.versioning.lastUpdated = "LAST-UPDATED"
        metadata.versioning.versions = arrayOf("1.0.0-RELEASE", "1.0.0-LATEST")

        assertThat(KeypleMarshaller().marshal(metadata).reader(StandardCharsets.UTF_8).readText())
                .isEqualTo("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                        "<metadata>" +
                            "<groupId>test-groupId</groupId>" +
                            "<artifactId>test-artifactId</artifactId>" +
                            "<versioning>" +
                                "<latest>1.0.0-LATEST</latest>" +
                                "<release>1.0.0-RELEASE</release>" +
                                "<versions>" +
                                    "<version>1.0.0-RELEASE</version>" +
                                    "<version>1.0.0-LATEST</version>" +
                                "</versions>" +
                                "<lastUpdated>LAST-UPDATED</lastUpdated>" +
                            "</versioning>" +
                        "</metadata>")
    }

    @Test
    fun unmarshal() {
        val metadataStream = URL("https://repo1.maven.org/maven2/org/eclipse/keyple/keyple-java-core/maven-metadata.xml")
                .openStream()
        val metadata = KeypleMarshaller().unmarshal(metadataStream, MavenGroupMetadata::class.java)
        assertThat(metadata.groupId).isEqualTo("org.eclipse.keyple")
        assertThat(metadata.artifactId).isEqualTo("keyple-java-core")
        assertThat(metadata.versioning.lastUpdated).matches("\\d+")
        assertThat(metadata.versioning.latest).matches("\\d+\\.\\d+\\.\\d+")
        assertThat(metadata.versioning.release).matches("\\d+\\.\\d+\\.\\d+")
        assertThat(metadata.versioning.versions).isNotEmpty()
    }
}