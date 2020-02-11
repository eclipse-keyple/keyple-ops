#!groovy
@Library('java-builder') _
pipeline {
    agent {
        kubernetes {
            label 'keyple-gradle'
            yaml javaBuilder('1')
        }
    }
    stages {
        stage('Keyple Gradle Plugin: Build and test') {
            steps{
                container('java-builder') {
                    configFileProvider(
                        [configFile(
                            fileId: 'gradle.properties',
                            targetLocation: '/home/jenkins/agent/gradle.properties')]) {
                        sh 'ln -s /home/jenkins/agent/gradle.properties /home/jenkins/.gradle/gradle.properties'
                        dir('java/keyple-gradle') {
                            sh './gradlew clean build test'
                        }
                    }
                }
            }
        }
        stage('Keyple Gradle Plugin: Upload to sonatype') {
            when {
                expression { env.GIT_URL == 'https://github.com/eclipse/keyple-ops.git' && env.GIT_BRANCH == "master" }
            }
            steps{
                container('java-builder') {
                    configFileProvider(
                        [configFile(
                            fileId: 'gradle.properties',
                            targetLocation: '/home/jenkins/agent/gradle.properties')]) {
                        dir('java/keyple-gradle') {
                            sh './gradlew uploadArchives'
                        }
                    }
                }
            }
        }
    }
}