# eclipsekeyple/java-builder:1
FROM openjdk:8-jdk
LABEL maintainer Brice Ruppen <brice.ruppen@armotic.fr>, Olivier Delcroix <odelcroi@gmail.com>

# PGP installation
RUN apt-get update \
    && apt-get remove -y  gnupg \
    && apt-get install -y gnupg1 tree \
    && apt-get clean \
    && echo -n "Successfully installed " \
    && gpg1 --version

# Gradle installation
ARG GRADLE_VERSION="4.5.1"
ENV GRADLE_HOME="/opt/gradle/gradle-${GRADLE_VERSION}"
ENV JAVA_OPTS="-Xmx1800m -XX:MaxPermSize=512m -Dfile.encoding=UTF-8 -XX:+HeapDumpOnOutOfMemoryError"
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
ARG ANDROID_SDK_ROOT="/opt/android-sdk"
ENV ANDROID_SDK_ROOT="${ANDROID_SDK_ROOT}"
ENV ANDROID_HOME="${ANDROID_SDK_ROOT}"
ENV ANDROID_SDK="${ANDROID_SDK_ROOT}"
RUN umask g+w \
 && curl -fLsSo sdk-tools.zip "https://dl.google.com/android/repository/sdk-tools-linux-4333796.zip" \
 && echo -n "Files extracted: " \
 && unzip -d "${ANDROID_SDK_ROOT}" sdk-tools.zip | grep -c 'inflating:' \
 && mkdir -p "${HOME}/.android" \
 && touch "${HOME}/.android/repositories.cfg" \
 && update-alternatives --install "/usr/bin/sdkmanager" sdkmanager "${ANDROID_SDK_ROOT}/tools/bin/sdkmanager" 1 \
 && yes | sdkmanager --sdk_root=${ANDROID_SDK_ROOT} --licenses \
 && rm -rf "sdk-tools.zip"

RUN umask g+w \
 && sdkmanager --sdk_root=${ANDROID_SDK_ROOT} tools \
 && yes | sdkmanager --sdk_root=${ANDROID_SDK_ROOT} --licenses

RUN umask g+w \
 && sdkmanager --sdk_root=${ANDROID_SDK_ROOT} 'build-tools;26.0.3' \
 && yes | sdkmanager --sdk_root=${ANDROID_SDK_ROOT} --licenses

RUN umask g+w \
 && sdkmanager --sdk_root=${ANDROID_SDK_ROOT} 'platforms;android-26' \
 && yes | sdkmanager --sdk_root=${ANDROID_SDK_ROOT} --licenses

RUN echo -n "Successfully installed " \
 && sdkmanager --list | sed -n '1,/Available/p' | head -n -2

# Workspace creation
RUN umask g+w \
 && mkdir -p "/var/build"
WORKDIR "/var/build"

### user name recognition at runtime w/ an arbitrary uid - for OpenShift deployments
COPY uid_entrypoint /usr/local/bin/uid_entrypoint
RUN chmod u+x /usr/local/bin/uid_entrypoint && \
    chgrp 0 /usr/local/bin/uid_entrypoint && \
    chmod g=u /usr/local/bin/uid_entrypoint /etc/passwd
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
 && mkdir -p ".gradle" ".android" \
 && touch ".android/repositories.cfg" 

#
# Pre-Download dependencies
#
RUN umask g+w \
 && git clone "https://github.com/eclipse/keyple-java.git" \
 && cd "keyple-java" \
 && gradle wrapper --gradle-version 4.5.1 \
 && set -x \
 && ./gradlew :java:component:keyple-core:uploadArchives --info \
 && ./gradlew :java:component:keyple-calypso:uploadArchives --info \
 && ./gradlew :java:component:keyple-plugin:keyple-plugin-remotese:assemble --info \
 && ./gradlew -b ./android/build.gradle :keyple-plugin:keyple-plugin-android-nfc:uploadArchives \
 && ./gradlew -b ./java/example/calypso/android/nfc/build.gradle assembleDebug \
 && ./gradlew --stop \
 && cd /var/build \
 && rm -rf "/var/build/keyple-java" "${GRADLE_USER_HOME}/daemon"

ENTRYPOINT [ "/usr/local/bin/uid_entrypoint" ]

LABEL version 1