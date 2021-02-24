package org.eclipse.keyple.gradle.pom

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.eclipse.keyple.gradle.title
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPom
import java.io.Closeable
import java.io.InputStream


class YamlToPom(val yaml: InputStream, val project: Project) : Closeable {

    private val mapper = ObjectMapper(YAMLFactory()).registerKotlinModule()

    fun inject(pom: MavenPom) {
        val root = mapper.readValue(yaml, PomRoot::class.java)
        pom.name.set(project.title)
        root.description?.let { pom.description.set(it) }
            ?: pom.description.set(project.description)
        root.url?.let { pom.url.set(it) }
        root.organization?.let { organization ->
            pom.organization { pomOrganization ->
                organization.name?.let { pomOrganization.name.set(it) }
                organization.url?.let { pomOrganization.url.set(it) }
            }
        }
        root.licenses?.let { licenses ->
            pom.licenses { pomLicences ->
                for (license in licenses) {
                    pomLicences.license { pomLicense ->
                        license.name?.let { pomLicense.name.set(it) }
                        license.url?.let { pomLicense.url.set(it) }
                        license.distribution?.let { pomLicense.distribution.set(it) }
                        license.comments?.let { pomLicense.comments.set(it) }
                    }
                }
            }
        }
        root.developers?.let { developers ->
            pom.developers { pomDevelopers ->
                for (developer in developers) {
                    pomDevelopers.developer { pomDeveloper ->
                        developer.id?.let { pomDeveloper.id.set(it) }
                        developer.name?.let { pomDeveloper.name.set(it) }
                        developer.email?.let { pomDeveloper.email.set(it) }
                        developer.url?.let { pomDeveloper.url.set(it) }
                        developer.organization?.let { pomDeveloper.organization.set(it) }
                        developer.organizationUrl?.let { pomDeveloper.organizationUrl.set(it) }
                        developer.timeZone?.let { pomDeveloper.timezone.set(it) }
                        developer.roles?.let { pomDeveloper.roles.set(it) }
                    }
                }
            }
        }
        root.contributors?.let { contributors ->
            pom.contributors { pomContributors ->
                for (contributor in contributors) {
                    pomContributors.contributor { pomContributor ->
                        contributor.name?.let { pomContributor.name.set(it) }
                        contributor.email?.let { pomContributor.email.set(it) }
                        contributor.url?.let { pomContributor.url.set(it) }
                        contributor.organization?.let { pomContributor.organization.set(it) }
                        contributor.organizationUrl?.let { pomContributor.organizationUrl.set(it) }
                        contributor.timeZone?.let { pomContributor.timezone.set(it) }
                        contributor.roles?.let { pomContributor.roles.set(it) }
                    }
                }
            }
        }
        root.scm?.let { scm ->
            pom.scm { pomScm ->
                scm.connection?.let { pomScm.connection.set(it) }
                scm.developerConnection?.let { pomScm.developerConnection.set(it) }
                scm.url?.let { pomScm.url.set(it) }
                scm.tag?.let { pomScm.tag.set(it) }
            }
        }
        root.distributionManagement?.let { distr ->
            pom.distributionManagement { pomDistr ->
                distr.downloadUrl?.let { pomDistr.downloadUrl.set(it) }
            }
        }
        root.issueManagement?.let { issue ->
            pom.issueManagement { pomIssue ->
                issue.system?.let { pomIssue.system.set(it) }
                issue.url?.let { pomIssue.url.set(it) }
            }
        }
        root.ciManagement?.let { ci ->
            pom.ciManagement { pomCi ->
                ci.system?.let { pomCi.system.set(it) }
                ci.url?.let { pomCi.url.set(it) }
            }
        }
        root.properties?.let { pom.properties.set(it) }
    }


    override fun close() {
        yaml.close()
    }


}