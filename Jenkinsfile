#!groovy
pipeline {
    agent {
        kubernetes {
            label 'keyple-gradle'
            yaml javaBuilder('1')
        }
    }
    stages {
        stage('Import keyring'){
            when {
                expression { env.GIT_URL == 'https://github.com/eclipse/keyple-ops.git' && env.GIT_BRANCH == "master" }
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