# eclipsekeyple/java-builder:1
FROM openjdk:8-jdk
LABEL maintainer Brice Ruppen <brice.ruppen@armotic.fr>
LABEL version 1

# PGP installation
RUN apt-get update \
    && apt-get remove -y  gnupg \
    && apt-get install -y gnupg1 \
    && apt-get clean \
    && echo -n "Successfully installed " \
    && gpg1 --version

# Gradle installation
ARG GRADLE_VERSION="4.5.1"
ENV GRADLE_HOME="/opt/gradle/gradle-${GRADLE_VERSION}"
RUN mkdir -p "/opt/gradle" \
 && cd "/opt/gradle" \
 && curl -fLsSo gradle.zip "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip" \
 && echo -n "Files extracted: " \
 && unzip -d /opt/gradle gradle.zip | grep -c 'inflating:' \
 && chmod u+x "${GRADLE_HOME}/bin/gradle" \
 && update-alternatives --install "/usr/bin/gradle" gradle "$GRADLE_HOME/bin/gradle" 1 \
 && rm -f "gradle.zip" \
 && echo -n "Successfully installed " \
 && gradle --version

# Android installation
ENV ANDROID_SDK="/opt/android-sdk"
RUN curl -fLsSo sdk-tools.zip "https://dl.google.com/android/repository/sdk-tools-linux-3859397.zip" \
 && echo -n "Files extracted: " \
 && unzip -d "${ANDROID_SDK}" sdk-tools.zip | grep -c 'inflating:' \
 && mkdir -p "~/.android" \
 && touch "~/.android/repositories.cfg" \
 && update-alternatives --install "/usr/bin/sdkmanager" sdkmanager "${ANDROID_SDK}/tools/bin/sdkmanager" 1 \
 && yes | sdkmanager --sdk_root=${ANDROID_SDK} --licenses \
 && rm -rf "tools" "sdk-tools.zip" \
 && echo -n "Successfully installed " \
 && sdkmanager --list | sed -n '1,/Available/p' | head -n -2

# Workspace creation
RUN umask g+w \
 && mkdir -p "/var/build"
WORKDIR "/var/build"

### user name recognition at runtime w/ an arbitrary uid - for OpenShift deployments
COPY uid_entrypoint /usr/local/bin/uid_entrypoint
RUN chmod +x /usr/local/bin/uid_entrypoint
### end

# Setting Jenkins user
RUN useradd --no-log-init -r -m -g root jenkins
USER jenkins:root
ARG USER="jenkins"
ENV USER_NAME="${USER}"
ENV HOME="/home/${USER}"
ENV GRADLE_USER_HOME="/home/${USER}/.gradle"

RUN umask g+w \
 && chmod g+w ~ \
 && cd "/home/${USER}" \
 && mkdir -p ".gradle" \
 && mkdir -p ".android" \
 && touch ".android/repositories.cfg" 

#
# Pre-Download dependencies
#
RUN umask g+w \
 && git clone "https://github.com/eclipse/keyple-java.git" \
 && cd "keyple-java" \
 && gradle wrapper --gradle-version 4.5.1 \
 && ./gradlew :java:component:keyple-core:uploadArchives --info \
 && cd /var/build \
 && rm -rf "/var/build/keyple-java"

ENTRYPOINT [ "/usr/local/bin/uid_entrypoint" ]
