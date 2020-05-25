# eclipsekeyple/cpp-builder:7.0
FROM rikorose/gcc-cmake:gcc-7
LABEL maintainer Brice Ruppen <brice.ruppen@armotic.fr>

USER root:root

# PGP installation
RUN apt-get update \
    && apt-get remove -y  gnupg \
    && apt-get install -y gnupg1 tree libpcsclite-dev \
    && apt-get clean \
    && echo -n "Successfully installed " \
    && gpg1 --version

# Add github publickey for ssh connection
RUN echo 'github.com,192.30.252.*,192.30.253.*,192.30.254.*,192.30.255.*,192.30.252.*,140.82.112.*,140.82.113.*,140.82.114.*,140.82.115.*,140.82.116.*,140.82.117.*,140.82.118.*,140.82.119.*,140.82.120.*,140.82.121.*,140.82.122.*,140.82.123.*,140.82.124.*,140.82.125.*,140.82.126.*,140.82.127.* AAAAB3NzaC1yc2EAAAABIwAAAQEAq2A7hRGmdnm9tUDbO9IDSwBK6TbQa+PXYPCPy6rbTrTtw7PHkccKrpp0yVhp5HdEIcKr6pLlVDBfOLX9QUsyCOV0wzfjIJNlGEYsdlLJizHhbn2mUjvSAHQqZETYP81eFzLQNnPHt4EVVUh7VfDESU84KezmD5QlWpXLmvU31/yMf+Se8xhHTvKSCZIFImWwoG6mbUoWf9nzpIoaSjB+weqqUUmpaaasXVal72J+UX2B+2RPW3RcT0eOzQgqlJL3RKrTJvdsjE3JEAvGq3lGHSZXy28G3skua2SmVi/w4yCE6gbODqnTWlg7+wC604ydGXA8VJiS5ap43JXiUFFAaQ==' >> /etc/ssh/ssh_known_hosts

RUN umask g+w \
	&& mkdir /opt/actions-runner \
	&& cd /opt/actions-runner \
	&& curl -LO https://github.com/actions/runner/releases/download/v2.168.0/actions-runner-linux-x64-2.168.0.tar.gz \
	&& tar xzf ./actions-runner-linux-x64-2.168.0.tar.gz \
	&& rm -f ./actions-runner-linux-x64-*.tar.gz \
	&& chmod 777 /opt/actions-runner \
	&& ls -l /opt

### user name recognition at runtime w/ an arbitrary uid - for OpenShift deployments
RUN mkdir /var/build \
 && chmod 777 /var/build \
 && ln -s /dev/null "/var/build/workspace"
COPY uid_entrypoint /usr/local/bin/uid_entrypoint
RUN chmod u+x /usr/local/bin/uid_entrypoint && \
    chgrp 0 /usr/local/bin/uid_entrypoint && \
    chmod g=u /usr/local/bin/uid_entrypoint /etc/passwd
### end

# Setting Builder user
ARG USER="builder"
RUN useradd --no-log-init -r -m -g root "${USER}"
USER ${USER}:root
ENV USER_NAME="${USER}"
ENV HOME="/home/${USER}"

ENTRYPOINT [ "/usr/local/bin/uid_entrypoint" ]
WORKDIR "/var/build"

LABEL version 7.0
