import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URL
import java.net.HttpURLConnection
import java.io.IOException

plugins {
    kotlin("jvm") version "1.7.10" // Update Kotlin version to match Gradle's
    signing
    jacoco
    id("org.sonarqube") version "3.0"
    `maven-publish`
}

buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.10") // Update Kotlin Gradle plugin version
        classpath("com.sun.istack:istack-commons-runtime:3.0.11")
    }
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation(gradleApi())
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.7.10")
    implementation("com.fasterxml.jackson.core:jackson-core:2.13.3")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.3")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.13.3")
    implementation("jakarta.xml.bind:jakarta.xml.bind-api:3.0.1")
    implementation("org.glassfish.jaxb:jaxb-runtime:3.0.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
    testImplementation("com.sun.istack:istack-commons-runtime:3.0.12")
    testImplementation("org.assertj:assertj-core:3.22.0")
    testImplementation("org.mockito:mockito-core:4.3.1")
}

val compileKotlin: KotlinCompile by tasks
val compileTestKotlin: KotlinCompile by tasks
val releaseRepo: String by project
val snapshotRepo: String by project
val ossrhUsername: String by project
val ossrhPassword: String by project

compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

fun canBeUploaded(project: Project): Boolean {
    val jarGroup = (project.group as String).replace('.', '/')
    val jarName = "${project.name}-${project.version}.jar"
    val repositoryPath = "$jarGroup/${project.name}/${project.version}/$jarName"
    val repositoryUrl = releaseRepo + repositoryPath

    val canBeUploaded = !urlExists(repositoryUrl)
    if (!canBeUploaded) {
        println("Artifacts already exists on repository, no need to upload it again.")
    } else {
        println("Artifacts can safely be uploaded.")
        println("No artifact at ${repositoryUrl}")
    }
    return canBeUploaded
}

fun urlExists(repositoryUrl: String): Boolean {
    return try {
        val connection = URL(repositoryUrl).openConnection() as HttpURLConnection
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000
        connection.requestMethod = "HEAD"
        connection.responseCode == 200
    } catch (ignored: IOException) {
        false
    }
}

task("install") {
    dependsOn("publishToMavenLocal")
}

val sourcesJar by tasks.creating(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}
val javadocJar by tasks.creating(Jar::class) {
    dependsOn.add("javadoc")
    archiveClassifier.set("javadoc")
    from("javadoc")
}

tasks {
    jar {
        manifest {
            attributes(
                Pair("Implementation-Title", project.name),
                Pair("Implementation-Version", project.version))
        }
    }
    javadoc {
        options.encoding = "UTF-8"
    }
    artifacts {
        archives(sourcesJar)
        archives(javadocJar)
        archives(jar)
    }
    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
        finalizedBy("jacocoTestReport")
    }
    jacocoTestReport {
        dependsOn("test")
        reports {
            xml.isEnabled = true
            csv.isEnabled = false
            html.isEnabled = true
        }
    }
    sonarqube {
        properties {
            property("sonar.projectKey", "eclipse_keyple-gradle")
            property("sonar.organization", "eclipse")
            property("sonar.host.url", "https://sonarcloud.io")
            property("sonar.login", System.getenv("SONAR_LOGIN"))
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["kotlin"])
            artifact(sourcesJar)
            artifact(javadocJar)
            pom {
                name.set("Keyple Gradle Plugin")
                description.set("Gradle Plugin that regroups common tasks used by all Keyple Projects.")
                url.set("https://projects.eclipse.org/projects/iot.keyple")
                organization {
                    name.set("Eclipse Keyple")
                    url.set("https://projects.eclipse.org/projects/iot.keyple")
                }
                licenses {
                    license {
                        name.set("Eclipse Public License - v 2.0")
                        url.set("https://www.eclipse.org/legal/epl-2.0/")
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        name.set("Olivier Delcroix")
                        email.set("odelcroi@gmail.com")
                    }
                    developer {
                        name.set("Brice Ruppen")
                        email.set("brice.ruppen@armotic.fr")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/eclipse-keyple/keyple-ops.git")
                    developerConnection.set("scm:git:https://github.com/eclipse-keyple/keyple-ops.git")
                    url.set("http://github.com/eclipse-keyple/keyple-ops/tree/master")
                }
            }
        }
    }
    repositories {
        maven {
            credentials {
                username = ossrhUsername
                password = ossrhPassword
            }
            if (version.toString().endsWith("-SNAPSHOT")) {
                url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
            } else if (canBeUploaded(project)) {
                url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            }
        }
    }
}

if (project.hasProperty("signing.keyId")) {
    signing {
        sign(publishing.publications["mavenJava"])
    }
}
