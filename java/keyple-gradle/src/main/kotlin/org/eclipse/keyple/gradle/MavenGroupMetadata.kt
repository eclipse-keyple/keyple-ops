package org.eclipse.keyple.gradle

import jakarta.xml.bind.annotation.XmlAccessType
import jakarta.xml.bind.annotation.XmlAccessorType
import jakarta.xml.bind.annotation.XmlElement
import jakarta.xml.bind.annotation.XmlElementWrapper
import jakarta.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "metadata")
@XmlAccessorType(XmlAccessType.FIELD)
class MavenGroupMetadata {
  var groupId = ""
  var artifactId = ""
  var versioning: MavenGroupVersioning = MavenGroupVersioning()
}

@XmlRootElement(name = "versioning")
@XmlAccessorType(XmlAccessType.FIELD)
class MavenGroupVersioning {
  var latest = ""
  var release = ""

  @field:[XmlElementWrapper(name = "versions") XmlElement(name = "version")]
  var versions: Array<String> = Array(0, { "" })
  var lastUpdated = ""
}
