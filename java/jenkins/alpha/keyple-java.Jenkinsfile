#!groovy
def keypleVersion
pipeline {
    agent {
        kubernetes {
            label 'keyple-java'
            yaml javaBuilder('1')
        }
    }
    environment {
        uploadParams = "-PdoSign=true --info"
        forceBuild = false
        PROJECT_NAME = "keyple-java"
        PROJECT_BOT_NAME = "Eclipse Keyple Bot" 
    }
    stages {
        stage('Keyple Java: Git checkout') {
            steps{
                container('java-builder') {
                    git url: 'https://github.com/eclipse/keyple-java.git',
                        credentialsId: 'github-bot',
                        branch: 'develop'
                }
            }
        }
        stage('Import keyring'){
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
        stage('Keyple Java: Build and Test next Alpha') {
            steps{
                container('java-builder') {
                    sh './gradlew setNextAlphaVersion'
                    sh './gradlew installAll --info'
                    sh './gradlew check --info'

                    script {
                        keypleVersion = sh(script: 'grep version java/component/keyple-core/gradle.properties | cut -d= -f2 | tr -d "[:space:]"', returnStdout: true).trim()
                        echo "Building version ${keypleVersion}"
                    }
                }
            }
        }
        stage('Keyple Android: Build and Test next Alpha') {
            steps{
                container('java-builder') {
                    dir('android') {
                        sh './gradlew setNextAlphaVersion'
                        sh './gradlew :keyple-plugin:keyple-plugin-android-nfc:installPlugin :keyple-plugin:keyple-plugin-android-nfc:check'
                        sh './gradlew :keyple-plugin:keyple-plugin-android-omapi:installPlugin :keyple-plugin:keyple-plugin-android-omapi:check'
                    }
                }
            }
        }
        stage('Keyple Java: Commit/Tag/Push') {
            steps{
                container('java-builder') {
                    withCredentials([usernamePassword(credentialsId: 'github-bot', passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
                        sh """
                            git add .
                            if ! git diff --cached --exit-code; then
                                git config --global user.email "${PROJECT_NAME}-bot@eclipse.org"
                                git config --global user.name "${PROJECT_BOT_NAME}"
                                git commit -m 'Release keyple-java ${keypleVersion}' --signoff
                                git tag '${keypleVersion}'
                                git push 'https://${GIT_USERNAME}:${GIT_PASSWORD}@github.com/eclipse/keyple-java.git' refs/tags/${keypleVersion}
                            else
                                echo 'No change have been detected since last build, nothing to publish'
                                exit 2
                            fi
                        """
                    }
                }
            }
        }
        stage('Keyple Java: Upload artifacts to sonatype') {
            steps{
                container('java-builder') {
                    configFileProvider(
                        [configFile(fileId: 'gradle.properties',
                            targetLocation: '/home/jenkins/agent/gradle.properties')]) {
                        sh './gradlew :java:component:keyple-core:uploadArchives ${uploadParams}'
                        sh './gradlew :java:component:keyple-calypso:uploadArchives ${uploadParams}'
                        sh './gradlew :java:component:keyple-plugin:keyple-plugin-pcsc:uploadArchives ${uploadParams}'
                        sh './gradlew :java:component:keyple-plugin:keyple-plugin-stub:uploadArchives ${uploadParams}'
                        sh './gradlew --stop'
                    }
                }
            }
        }
        stage('Keyple Android: Upload artifacts to sonatype') {
            steps{
                container('java-builder') {
                    configFileProvider(
                        [configFile(fileId: 'gradle.properties',
                            targetLocation: '/home/jenkins/agent/gradle.properties')]) {
                         dir('android') {
                            sh "./gradlew :keyple-plugin:keyple-plugin-android-nfc:uploadArchives ${uploadParams} -P keyple_version=${keypleVersion}"
                            sh "./gradlew :keyple-plugin:keyple-plugin-android-omapi:uploadArchives ${uploadParams} -P keyple_version=${keypleVersion}"
                            sh './gradlew --stop'
                        }
                    }
                }
            }
        }
        stage('Keyple Java: Generate apks') {
            steps{
                container('java-builder') {
                    sh 'keytool -genkey -v -keystore ~/.android/debug.keystore -storepass android -alias androiddebugkey -keypass android -dname "CN=Android Debug,O=Android,C=US" -keyalg RSA -keysize 2048 -validity 90'
                    dir('java/example/calypso/android/nfc/') {
                        sh "./gradlew assembleDebug -P keyple_version=${keypleVersion}"
                    }
                    dir('java/example/calypso/android/omapi') {
                        sh "./gradlew assembleDebug -P keyple_version=${keypleVersion}"
                    }
                }
            }
        }
        stage('Keyple Java: Deploy to eclipse') {
            steps {
                container('java-builder') {
                    sh 'mkdir ./repository'
                    sh 'mkdir ./repository/java'
                    sh 'mkdir ./repository/android'
                    sh 'cp ./java/component/keyple-calypso/build/libs/keyple-java-calypso*.jar ./repository/java'
                    sh 'cp ./java/component/keyple-core/build/libs/keyple-java-core*.jar ./repository/java'
                    sh 'cp ./java/component/keyple-plugin/pcsc/build/libs/keyple-java-plugin*.jar ./repository/java'
                    sh 'cp ./java/component/keyple-plugin/stub/build/libs/keyple-java-plugin*.jar ./repository/java'
                    sh 'cp ./java/example/calypso/android/nfc/build/outputs/apk/debug/*.apk ./repository/android'
                    sh 'cp ./java/example/calypso/android/omapi/build/outputs/apk/debug/*.apk ./repository/android'
                    sh 'cp ./android/keyple-plugin/android-nfc/build/outputs/aar/keyple-android-plugin*.aar ./repository/android'
                    sh 'cp ./android/keyple-plugin/android-omapi/build/outputs/aar/keyple-android-plugin*.aar ./repository/android'
                    sh 'ls -R ./repository'
                    sshagent(['projects-storage.eclipse.org-bot-ssh']) {
                        //sh "head -n 50 /etc/passwd"
                        sh "ssh genie.keyple@projects-storage.eclipse.org rm -rf /home/data/httpd/download.eclipse.org/keyple/releases"
                        sh "ssh genie.keyple@projects-storage.eclipse.org mkdir -p /home/data/httpd/download.eclipse.org/keyple/releases"
                        sh "scp -r ./repository/* genie.keyple@projects-storage.eclipse.org:/home/data/httpd/download.eclipse.org/keyple/releases"
                    }
                }
            }
        }
    }
    post { 
        always {
            container('java-builder') {
                archiveArtifacts artifacts: 'java/component/**/build/reports/tests/**,android/keyple-plugin/**/build/reports/tests/**', allowEmptyArchive: true
                junit allowEmptyResults: true, testResults: 'java/component/**/build/test-results/test/*.xml'
                junit allowEmptyResults: true, testResults: 'android/keyple-plugin/**/build/test-results/testDebugUnitTest/*.xml'
            }
        }
    }
}
