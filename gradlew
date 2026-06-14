#!/bin/bash
# Custom Gradle wrapper — downloads Gradle 8.4 if not cached

GRADLE_VERSION="8.4"
GRADLE_CACHE="/home/runner/.gradle-dist/gradle-${GRADLE_VERSION}"
GRADLE_BIN="${GRADLE_CACHE}/bin/gradle"

if [ ! -f "${GRADLE_BIN}" ]; then
    echo "[gradlew] Downloading Gradle ${GRADLE_VERSION} (first run, ~130MB)..."
    mkdir -p "/home/runner/.gradle-dist"
    wget -q "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip" \
         -O /tmp/gradle-${GRADLE_VERSION}.zip
    unzip -q /tmp/gradle-${GRADLE_VERSION}.zip -d "/home/runner/.gradle-dist/"
    rm -f /tmp/gradle-${GRADLE_VERSION}.zip
    chmod +x "${GRADLE_BIN}"
    echo "[gradlew] Gradle ${GRADLE_VERSION} ready"
fi

export JAVA_HOME="$(dirname $(dirname $(readlink -f $(which java))))"
exec "${GRADLE_BIN}" "$@"
