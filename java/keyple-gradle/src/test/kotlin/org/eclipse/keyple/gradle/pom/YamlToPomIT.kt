package org.eclipse.keyple.gradle.pom

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.entry
import org.eclipse.keyple.gradle.title
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.XmlProvider
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.publish.maven.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.util.concurrent.atomic.AtomicInteger

@Suppress("UNCHECKED_CAST")
fun checkPropertyEquals(expected: String): Property<String> {
    val mock = mock(Property::class.java) as Property<String>
    doAnswer { assertThat(it.getArgument(0) as String).isEqualTo(expected) }
        .`when`(mock).set(anyString())
    return mock;
}

@Suppress("UNCHECKED_CAST")
fun checkPropertyContains(vararg expected: String): SetProperty<String> {
    val mock = mock(SetProperty::class.java) as SetProperty<String>
    doAnswer { assertThat(it.getArgument(0) as Set<String>).contains(*expected) }
        .`when`(mock).set(anySet())
    return mock;
}


internal class YamlToPomIT {

    @Test
    fun transformYamlToPom() {
        val mavenPomMock = MavenPomMock();
        val project = mock(Project::class.java)
        doReturn("IT Name").`when`(project).name
        doReturn("IT Title").`when`(project).title
        doReturn("Description Fallback").`when`(project).description
        doAnswer { a -> "Property ${a.getArgument<String>(0)}" }
            .`when`(project)
            .property(anyString())

        YamlToPom(javaClass.getResourceAsStream("POM.yml")!!, project)
            .use {
                it.inject(mavenPomMock);
            }

        assertThat(mavenPomMock.properties).contains(
            entry("key1", "value1"),
            entry("key2", "value2"),
            entry("key3", "value3")
        )
    }

    @Suppress("UNCHECKED_CAST")
    class MavenPomMock : MavenPom {

        override fun getPackaging(): String {
            TODO("Not yet implemented")
        }

        override fun setPackaging(packaging: String?) {
            TODO("Not yet implemented")
        }

        override fun getName(): Property<String> {
            return checkPropertyEquals("Property title")
        }

        override fun getDescription(): Property<String> {
            return checkPropertyEquals("Description of the project")
        }

        override fun getUrl(): Property<String> {
            return checkPropertyEquals("Url of the project")
        }

        override fun getInceptionYear(): Property<String> {
            TODO("Not yet implemented")
        }

        override fun licenses(action: Action<in MavenPomLicenseSpec>?) {
            action?.execute(object : MavenPomLicenseSpec {
                override fun license(action: Action<in MavenPomLicense>?) {
                    action?.execute(object : MavenPomLicense {
                        override fun getName(): Property<String> {
                            return checkPropertyEquals("Name of the license")
                        }

                        override fun getUrl(): Property<String> {
                            return checkPropertyEquals("Url of the license")
                        }

                        override fun getDistribution(): Property<String> {
                            return checkPropertyEquals("Type of distribution of the license")
                        }

                        override fun getComments(): Property<String> {
                            TODO("Not yet implemented")
                        }
                    })
                }
            })
        }

        override fun organization(action: Action<in MavenPomOrganization>?) {
            action?.execute(object : MavenPomOrganization {
                override fun getName(): Property<String> {
                    return checkPropertyEquals("Name of the Organization")
                }

                override fun getUrl(): Property<String> {
                    return checkPropertyEquals("Website of the Organization")
                }
            })
        }

        override fun developers(action: Action<in MavenPomDeveloperSpec>?) {
            action?.execute(object : MavenPomDeveloperSpec {
                val developersIndex = AtomicInteger()
                override fun developer(action: Action<in MavenPomDeveloper>?) {
                    action?.execute(object : MavenPomDeveloper {
                        val index = developersIndex.incrementAndGet();
                        override fun getId(): Property<String> {
                            return checkPropertyEquals("dev$index")
                        }

                        override fun getName(): Property<String> {
                            return checkPropertyEquals("Name of developer $index")
                        }

                        override fun getEmail(): Property<String> {
                            return checkPropertyEquals("Email of developer $index")
                        }

                        override fun getUrl(): Property<String> {
                            return checkPropertyEquals("Website of developer $index")
                        }

                        override fun getOrganization(): Property<String> {
                            return checkPropertyEquals("Organization of developer $index")
                        }

                        override fun getOrganizationUrl(): Property<String> {
                            return checkPropertyEquals("Organization website of developer $index")
                        }

                        override fun getRoles(): SetProperty<String> {
                            return checkPropertyContains("Lead dev", "DevOps")
                        }

                        override fun getTimezone(): Property<String> {
                            return checkPropertyEquals("TimeZone of developer $index")
                        }

                        override fun getProperties(): MapProperty<String, String> {
                            TODO("Not yet implemented")
                        }
                    })
                }
            })
        }

        override fun contributors(action: Action<in MavenPomContributorSpec>?) {
            action?.execute(object : MavenPomContributorSpec {
                val contributorsIndex = AtomicInteger()
                override fun contributor(action: Action<in MavenPomContributor>?) {
                    action?.execute(object : MavenPomContributor {
                        val index = contributorsIndex.incrementAndGet();
                        override fun getName(): Property<String> {
                            return checkPropertyEquals("Name of contributor $index")
                        }

                        override fun getEmail(): Property<String> {
                            return checkPropertyEquals("Email of contributor $index")
                        }

                        override fun getUrl(): Property<String> {
                            return checkPropertyEquals("Website of contributor $index")
                        }

                        override fun getOrganization(): Property<String> {
                            return checkPropertyEquals("Organization of contributor $index")
                        }

                        override fun getOrganizationUrl(): Property<String> {
                            return checkPropertyEquals("Organization website of contributor $index")
                        }

                        override fun getRoles(): SetProperty<String> {
                            return checkPropertyContains("Dev Android")
                        }

                        override fun getTimezone(): Property<String> {
                            return checkPropertyEquals("TimeZone of contributor $index")
                        }

                        override fun getProperties(): MapProperty<String, String> {
                            TODO("Not yet implemented")
                        }
                    })
                }
            })
        }

        override fun scm(action: Action<in MavenPomScm>?) {
            action?.execute(object : MavenPomScm {
                override fun getConnection(): Property<String> {
                    return checkPropertyEquals("Connection to access the repository")
                }

                override fun getDeveloperConnection(): Property<String> {
                    return checkPropertyEquals("Connection used by a developer to access the repository")
                }

                override fun getUrl(): Property<String> {
                    return checkPropertyEquals("Url of the repository")
                }

                override fun getTag(): Property<String> {
                    return checkPropertyEquals("Tag of the repository")
                }

            })
        }

        override fun issueManagement(action: Action<in MavenPomIssueManagement>?) {
            action?.execute(object : MavenPomIssueManagement {
                override fun getSystem(): Property<String> {
                    return checkPropertyEquals("Issue system")
                }

                override fun getUrl(): Property<String> {
                    return checkPropertyEquals("Url of Issue system")
                }
            })
        }

        override fun ciManagement(action: Action<in MavenPomCiManagement>?) {
            action?.execute(object : MavenPomCiManagement {
                override fun getSystem(): Property<String> {
                    return checkPropertyEquals("CI system")
                }

                override fun getUrl(): Property<String> {
                    return checkPropertyEquals("Url of CI system")
                }
            })
        }

        override fun distributionManagement(action: Action<in MavenPomDistributionManagement>?) {
            action?.execute(object : MavenPomDistributionManagement {
                override fun getDownloadUrl(): Property<String> {
                    return checkPropertyEquals("Url where the artifacts can be downloaded")
                }

                override fun relocation(action: Action<in MavenPomRelocation>?) {
                    TODO("Not yet implemented")
                }
            })
        }

        override fun mailingLists(action: Action<in MavenPomMailingListSpec>?) {
            TODO("Not yet implemented")
        }

        var properties: Map<String, String>? = null

        override fun getProperties(): MapProperty<String, String> {
            val mock = mock(MapProperty::class.java) as MapProperty<String, String>
            doAnswer { (it.arguments[0] as Map<String, String>?).also { properties = it } }
                .`when`(mock).set(anyMap())
            return mock;
        }

        override fun withXml(action: Action<in XmlProvider>?) {
            TODO("Not yet implemented")
        }
    }
}