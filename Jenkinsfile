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
                    withCredentials([
                        file(credentialsId: 'secret-subkeys.asc',
                            variable: 'KEYRING')]) {
                        sh 'ln -s /home/jenkins/agent/gradle.properties /home/jenkins/.gradle/gradle.properties'
                        
                        /* Import GPG keyring with --batch and trust the keys non-interactively in a shell build step */
                        sh 'gpg1 --batch --import "${KEYRING}"'
                        sh 'gpg1 --list-secret-keys'
                        sh 'gpg1 --list-keys'
                        sh 'gpg1 --version'
                        sh 'for fpr in $(gpg1 --list-keys --with-colons  | awk -F: \'/fpr:/ {print $10}\' | sort -u); do echo -e "5\ny\n" |  gpg1 --batch --command-fd 0 --expert --edit-key ${fpr} trust; done'
                        sh 'ls -l  /home/jenkins/.gnupg/'
                    }
                    configFileProvider(
                        [configFile(fileId: 'gradle.properties',
                            targetLocation: '/home/jenkins/agent/gradle.properties')]) {
                        /* Read key Id in gradle.properties */
                        sh 'head -1 /home/jenkins/.gradle/gradle.properties'
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
                            sh './gradlew clean build'
                        }
                        catchError(buildResult: 'UNSTABLE', message: 'There were failing tests.', stageResult: 'UNSTABLE') {
                            dir('java/keyple-gradle') {
                                sh './gradlew test'
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
                            sh './gradlew uploadArchives'
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
                                sh './gradlew sonarqube'
                            }
                        }
                    }
                }
            }
        }
    }
}