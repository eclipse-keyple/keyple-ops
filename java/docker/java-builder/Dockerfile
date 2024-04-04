# eclipsekeyple/java-builder:2.0
FROM eclipse-temurin:17-jdk-focal

LABEL maintainer Jean-Pierre Fortune <jean-pierre.fortune@ialto.com>, Andrei Cristea <andrei.cristea019@gmail.com>

# PGP installation
RUN apt-get update \
    && apt-get remove -y  gnupg \
    && apt-get install -y dos2unix unzip git-lfs gnupg1 tree \
    && apt-get clean \
    && echo -n "Successfully installed " \
    && gpg1 --version
COPY import_gpg /usr/local/bin/import_gpg
RUN dos2unix /usr/local/bin/import_gpg

# Gradle installation
ARG GRADLE_VERSION="7.6.4"
ENV GRADLE_HOME="/opt/gradle/gradle-${GRADLE_VERSION}"
ENV JAVA_OPTS="-Xmx1800m -Dfile.encoding=UTF-8 -XX:+HeapDumpOnOutOfMemoryError -XX:+IgnoreUnrecognizedVMOptions"
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
 && curl -fLsSo commandlinetools.zip "https://dl.google.com/android/repository/commandlinetools-linux-6858069_latest.zip" \
 && echo -n "Files extracted: " \
 && unzip -d "${ANDROID_SDK_ROOT}" commandlinetools.zip | grep -c 'extracting:' \
 && mkdir -p "${HOME}/.android" \
 && touch "${HOME}/.android/repositories.cfg" \
 && update-alternatives --install "/usr/bin/sdkmanager" sdkmanager "${ANDROID_SDK_ROOT}/cmdline-tools/bin/sdkmanager" 1 \
 && yes | sdkmanager --sdk_root=${ANDROID_SDK_ROOT} --licenses \
 && rm -rf "commandlinetools.zip"

RUN umask g+w \
 && sdkmanager --sdk_root=${ANDROID_SDK_ROOT} tools \
 && yes | sdkmanager --sdk_root=${ANDROID_SDK_ROOT} --licenses

RUN umask g+w \
 && sdkmanager --sdk_root=${ANDROID_SDK_ROOT} 'build-tools;30.0.2' \
 && yes | sdkmanager --sdk_root=${ANDROID_SDK_ROOT} --licenses

RUN umask g+w \
 && sdkmanager --sdk_root=${ANDROID_SDK_ROOT} 'platforms;android-30' \
 && yes | sdkmanager --sdk_root=${ANDROID_SDK_ROOT} --licenses

RUN echo -n "Successfully installed " \
 && sdkmanager --sdk_root=${ANDROID_SDK_ROOT} --list | sed -n '1,/Available/p' | head -n -2

# GIT installation
RUN apt-get install git-lfs

# Workspace creation
RUN umask g+w \
 && mkdir -p "/var/build"
WORKDIR "/var/build"

# Add github publickey for ssh connection
RUN echo 'github.com,192.30.252.*,192.30.253.*,192.30.254.*,192.30.255.*,192.30.252.*,140.82.112.*,140.82.113.*,140.82.114.*,140.82.115.*,140.82.116.*,140.82.117.*,140.82.118.*,140.82.119.*,140.82.120.*,140.82.121.*,140.82.122.*,140.82.123.*,140.82.124.*,140.82.125.*,140.82.126.*,140.82.127.* AAAAB3NzaC1yc2EAAAABIwAAAQEAq2A7hRGmdnm9tUDbO9IDSwBK6TbQa+PXYPCPy6rbTrTtw7PHkccKrpp0yVhp5HdEIcKr6pLlVDBfOLX9QUsyCOV0wzfjIJNlGEYsdlLJizHhbn2mUjvSAHQqZETYP81eFzLQNnPHt4EVVUh7VfDESU84KezmD5QlWpXLmvU31/yMf+Se8xhHTvKSCZIFImWwoG6mbUoWf9nzpIoaSjB+weqqUUmpaaasXVal72J+UX2B+2RPW3RcT0eOzQgqlJL3RKrTJvdsjE3JEAvGq3lGHSZXy28G3skua2SmVi/w4yCE6gbODqnTWlg7+wC604ydGXA8VJiS5ap43JXiUFFAaQ==' >> /etc/ssh/ssh_known_hosts
COPY publish_packaging /usr/local/bin/publish_packaging
RUN dos2unix /usr/local/bin/publish_packaging

### user name recognition at runtime w/ an arbitrary uid - for OpenShift deployments
COPY uid_entrypoint /usr/local/bin/uid_entrypoint
RUN dos2unix /usr/local/bin/uid_entrypoint
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
 && git lfs install \
 && chmod g+w ~ \
 && cd "/home/${USER}" \
 && mkdir -p ".gradle" ".android" \
 && touch ".android/repositories.cfg" \
 && mkdir -p /home/jenkins/agent/ /home/jenkins/.gradle \
 && touch /home/jenkins/agent/gradle.properties \
 && ln -sf /home/jenkins/agent/gradle.properties /home/jenkins/.gradle/gradle.properties \
 && rm -rf /home/jenkins/agent

ENTRYPOINT [ "/usr/local/bin/uid_entrypoint" ]

LABEL version 2