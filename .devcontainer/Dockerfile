# Dev image for ParadisuPlugin: JetBrains Runtime 25 (hot code replace), Node 24 + pnpm (dev tool), build/debug tooling.
# Build context is the repository root.
FROM debian:trixie-slim

ARG TARGETARCH
ARG USERNAME=vscode
ARG USER_UID=1000
ARG USER_GID=${USER_UID}
# https://github.com/JetBrains/JetBrainsRuntime/releases (jbr-release-<JBR_VERSION>b<JBR_BUILD>)
ARG JBR_VERSION=25.0.4
ARG JBR_BUILD=b508.27
# https://nodejs.org/dist/index.json (keep the 24.x line; dev/package.json requires >=24)
ARG NODE_VERSION=24.21.0
# Must equal "packageManager" in dev/package.json
ARG PNPM_VERSION=12.11.2
# https://github.com/HotswapProjects/HotswapAgent/releases
ARG HOTSWAP_AGENT_VERSION=2.0.3
# https://github.com/zellij-org/zellij/releases (the default build includes the web client; not the no-web one)
ARG ZELLIJ_VERSION=0.45.1

ENV DEBIAN_FRONTEND=noninteractive \
    LANG=C.UTF-8 \
    JAVA_HOME=/opt/jbr \
    PATH=/opt/jbr/bin:/opt/node/bin:${PATH}

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        ca-certificates curl git jq openssl procps less unzip xz-utils sudo \
        postgresql-client openssh-client gnupg locales iproute2 netcat-openbsd \
    && rm -rf /var/lib/apt/lists/*

RUN groupadd --gid ${USER_GID} ${USERNAME} \
    && useradd --uid ${USER_UID} --gid ${USER_GID} -m -s /bin/bash ${USERNAME} \
    && echo "${USERNAME} ALL=(root) NOPASSWD:ALL" > /etc/sudoers.d/${USERNAME} \
    && chmod 0440 /etc/sudoers.d/${USERNAME}

# Mount points for the Gradle project caches (see docker-compose.yml). New volumes inherit their owner and mode,
# and the devcontainer CLI renumbers the user to the host user's UID (for example 1001 on GitHub runners), which
# would lock that user out of a volume owned by UID 1000: so make them writable by anyone.
RUN mkdir -p /workspaces/ParadisuPlugin/.gradle /workspaces/ParadisuPlugin/build-logic/.gradle \
    && chown -R ${USERNAME}:${USER_GID} /workspaces \
    && chmod 0777 /workspaces/ParadisuPlugin/.gradle /workspaces/ParadisuPlugin/build-logic/.gradle

# JetBrains Runtime SDK (the SDK variant includes javac, which the Gradle toolchain needs)
RUN case "${TARGETARCH}" in amd64) arch=x64 ;; arm64) arch=aarch64 ;; *) echo "unsupported arch ${TARGETARCH}" >&2; exit 1 ;; esac \
    && file="jbrsdk-${JBR_VERSION}-linux-${arch}-${JBR_BUILD}.tar.gz" \
    && base="https://cache-redirector.jetbrains.com/intellij-jbr" \
    && curl -fsSL -o "/tmp/${file}" "${base}/${file}" \
    && curl -fsSL -o "/tmp/${file}.checksum" "${base}/${file}.checksum" \
    && (cd /tmp && sha512sum -c "${file}.checksum") \
    && mkdir /opt/jbr \
    && tar -xzf "/tmp/${file}" -C /opt/jbr --strip-components=1 \
    && rm -f /tmp/jbrsdk-*

# HotswapAgent (only used when hotswap_agent = true in dev/dev.toml)
RUN mkdir -p /opt/jbr/lib/hotswap \
    && curl -fsSL -o /opt/jbr/lib/hotswap/hotswap-agent.jar \
        "https://github.com/HotswapProjects/HotswapAgent/releases/download/RELEASE-${HOTSWAP_AGENT_VERSION}/hotswap-agent-${HOTSWAP_AGENT_VERSION}.jar"

# zellij: terminal workspace for the server consoles (`dev ui`), with a web client (`dev web`)
RUN case "${TARGETARCH}" in amd64) arch=x86_64 ;; arm64) arch=aarch64 ;; *) echo "unsupported arch ${TARGETARCH}" >&2; exit 1 ;; esac \
    && file="zellij-${arch}-unknown-linux-musl.tar.gz" \
    && base="https://github.com/zellij-org/zellij/releases/download/v${ZELLIJ_VERSION}" \
    && curl -fsSL -o "/tmp/${file}" "${base}/${file}" \
    && curl -fsSL -o /tmp/zellij.sha256sum "${base}/zellij-${arch}-unknown-linux-musl.sha256sum" \
    && mkdir /tmp/zellij-x && tar -xzf "/tmp/${file}" -C /tmp/zellij-x \
    && echo "$(cut -d' ' -f1 /tmp/zellij.sha256sum)  /tmp/zellij-x/zellij" | sha256sum -c - \
    && install -m 0755 /tmp/zellij-x/zellij /usr/local/bin/zellij \
    && rm -rf /tmp/zellij* \
    && zellij --version

# GitHub CLI, from the official apt repository (https://github.com/cli/cli/blob/trunk/docs/install_linux.md)
RUN mkdir -p -m 755 /etc/apt/keyrings \
    && curl -fsSL -o /etc/apt/keyrings/githubcli-archive-keyring.gpg https://cli.github.com/packages/githubcli-archive-keyring.gpg \
    && chmod go+r /etc/apt/keyrings/githubcli-archive-keyring.gpg \
    && echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/githubcli-archive-keyring.gpg] https://cli.github.com/packages stable main" \
        > /etc/apt/sources.list.d/github-cli.list \
    && apt-get update \
    && apt-get install -y --no-install-recommends gh \
    && rm -rf /var/lib/apt/lists/* \
    && gh --version

# Node.js (runs the TypeScript dev tool natively) + pnpm via corepack
RUN case "${TARGETARCH}" in amd64) arch=x64 ;; arm64) arch=arm64 ;; *) echo "unsupported arch ${TARGETARCH}" >&2; exit 1 ;; esac \
    && file="node-v${NODE_VERSION}-linux-${arch}.tar.xz" \
    && curl -fsSL -o "/tmp/${file}" "https://nodejs.org/dist/v${NODE_VERSION}/${file}" \
    && curl -fsSL -o /tmp/SHASUMS256.txt "https://nodejs.org/dist/v${NODE_VERSION}/SHASUMS256.txt" \
    && (cd /tmp && grep " ${file}\$" SHASUMS256.txt | sha256sum -c -) \
    && mkdir /opt/node \
    && tar -xJf "/tmp/${file}" -C /opt/node --strip-components=1 \
    && rm -f /tmp/node-v* /tmp/SHASUMS256.txt \
    && ln -s /opt/node/bin/node /usr/local/bin/node \
    && ln -s /opt/node/bin/npm /usr/local/bin/npm \
    && ln -s /opt/node/bin/npx /usr/local/bin/npx \
    && ln -s /opt/node/bin/corepack /usr/local/bin/corepack

# Login shells (bash -l, VS Code terminals) reset PATH from /etc/profile, so ENV alone is not enough
RUN printf '%s\n' \
        'export JAVA_HOME=/opt/jbr' \
        'export PATH="/opt/jbr/bin:$HOME/.local/bin:$PATH"' \
        > /etc/profile.d/paradisu.sh

USER ${USERNAME}
WORKDIR /home/${USERNAME}

# corepack shims go in ~/.local/bin so no root is needed
ENV PATH=/home/${USERNAME}/.local/bin:${PATH}
RUN mkdir -p ~/.local/bin ~/.gradle \
    && corepack enable --install-directory ~/.local/bin \
    && corepack prepare "pnpm@${PNPM_VERSION}" --activate \
    && pnpm --version \
    && printf '%s\n' \
        'org.gradle.java.installations.paths=/opt/jbr' \
        'org.gradle.java.installations.auto-download=false' \
        'org.gradle.java.installations.auto-detect=false' \
        > ~/.gradle/gradle.properties

# Pre-fetch the Gradle distribution pinned by the wrapper
COPY --chown=${USERNAME}:${USER_GID} gradlew /tmp/gw/gradlew
COPY --chown=${USERNAME}:${USER_GID} gradle/wrapper /tmp/gw/gradle/wrapper
# A Windows checkout with core.autocrlf may have turned the script into CRLF (see .gitattributes), which fails with
# "gradlew: not found"; strip it so the image builds anyway
RUN sed -i 's/\r$//' /tmp/gw/gradlew \
    && chmod +x /tmp/gw/gradlew \
    && /tmp/gw/gradlew --version \
    && rm -rf /tmp/gw

# `dev` works from any directory once the workspace is mounted
RUN sudo ln -s /workspaces/ParadisuPlugin/dev/dev /usr/local/bin/dev
