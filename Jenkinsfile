#!groovy
pipeline {
    agent {
        kubernetes {
            label 'keyple-gradle'
            yaml javaBuilder('2.0')
        }
    }
    stages {
        stage('Prepare settings') {
            steps{
                container('java-builder') {
                    script {
                        keypleGradleVersion = sh(script: 'grep "^version" java/keyple-gradle/gradle.properties | cut -d= -f2 | tr -d "[:space:]"', returnStdout: true).trim()
                        deployKeypleGradle = env.GIT_URL == 'https://github.com/eclipse/keyple-ops.git' && env.GIT_BRANCH == "master" && env.CHANGE_ID == null
                    }
                }
            }
        }
        stage('Import keyring'){
            when {
                expression { deployKeypleGradle }
            }
            steps{
                container('java-builder') {
                    withCredentials([file(credentialsId: 'secret-subkeys.asc', variable: 'KEYRING')]) {
                        sh 'import_gpg "${KEYRING}"'
                    }
                }
            }
        }
        stage('Keyple Gradle Plugin: Build and test') {
            steps{
                container('java-builder') {
                    configFileProvider(
                        [configFile(
                            fileId: 'gradle.properties',
                            targetLocation: '/home/jenkins/agent/gradle.properties')]) {
                        dir('java/keyple-gradle') {
                            sh './gradlew clean assemble --info --stacktrace'
                        }
                        catchError(buildResult: 'UNSTABLE', message: 'There were failing tests.', stageResult: 'UNSTABLE') {
                            dir('java/keyple-gradle') {
                                sh './gradlew test --info --stacktrace'
                            }
                        }
                        junit allowEmptyResults: true, testResults: 'java/keyple-gradle/build/test-results/test/*.xml'
                    }
                }
            }
        }
        stage('Keyple Gradle Plugin: Upload to sonatype') {
            when {
                expression { deployKeypleGradle }
            }
            steps{
                container('java-builder') {
                    configFileProvider([configFile(
                            fileId: 'gradle.properties',
                            targetLocation: '/home/jenkins/agent/gradle.properties')]) {
                        dir('java/keyple-gradle') {
                            sh './gradlew publish --info --stacktrace'
                        }
                    }
                }
            }
        }
        stage('Keyple Gradle Plugin: Code Quality') {
            when {
                expression { deployKeypleGradle }
            }
            steps {
                catchError(buildResult: 'SUCCESS', message: 'Unable to log code quality to Sonar.', stageResult: 'FAILURE') {
                    container('java-builder') {
                        withCredentials([string(credentialsId: 'sonarcloud-token', variable: 'SONAR_LOGIN')]) {
                            dir('java/keyple-gradle') {
                                sh './gradlew sonarqube --info --stacktrace'
                            }
                        }
                    }
                }
            }
        }
    }
}