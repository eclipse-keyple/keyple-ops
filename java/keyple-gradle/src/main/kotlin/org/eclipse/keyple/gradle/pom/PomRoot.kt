package org.eclipse.keyple.gradle.pom

data class PomRoot(
    val description: String?,
    val url: String?,
    val organization: PomOrganization?,
    val licenses: List<PomLicense>?,
    val developers: List<PomContributor>?,
    val contributors: List<PomContributor>?,
    val scm: PomScm?,
    val distributionManagement: PomDistributionManagement?,
    val issueManagement: PomManagement?,
    val ciManagement: PomManagement?,
    val properties: Map<String, String>?
)

data class PomOrganization(
    val name: String?,
    val url: String?
)

data class PomLicense(
    val name: String?,
    val url: String?,
    val distribution: String?,
    val comments: String?
)

data class PomContributor(
    val id: String?,
    val name: String?,
    val email: String?,
    val url: String?,
    val organization: String?,
    val organizationUrl: String?,
    val timeZone: String?,
    val roles: List<String>?
)

data class PomScm(
    val connection: String?,
    val developerConnection: String?,
    val url: String?,
    val tag: String?
)

data class PomDistributionManagement(
    val downloadUrl: String?
)

data class PomManagement(
    val system: String?,
    val url: String?
)
