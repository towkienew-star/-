#!/usr/bin/env bash
set -euo pipefail
if command -v gradle >/dev/null; then
    gradle build --no-daemon
else
    mkdir -p .buildtools
    curl -L --fail --output .buildtools/gradle.zip https://services.gradle.org/distributions/gradle-8.13-bin.zip
    unzip -q -o .buildtools/gradle.zip -d .buildtools
    .buildtools/gradle-8.13/bin/gradle build --no-daemon
fi
echo 'JAR: build/libs/towkiemorphs-1.21.4-1.0.0.jar'
