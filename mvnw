#!/bin/sh
# Maven Wrapper script for Unix-based systems
# Downloads and runs Maven if not already available

set -e

MAVEN_PROJECTBASEDIR="${MAVEN_BASEDIR:-$(cd "$(dirname "$0")" && pwd)}"
MAVEN_WRAPPER_PROPERTIES_PATH="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.properties"

if [ -f "$MAVEN_WRAPPER_PROPERTIES_PATH" ]; then
    distributionUrl=$(grep 'distributionUrl' "$MAVEN_WRAPPER_PROPERTIES_PATH" | cut -d'=' -f2-)
fi

MAVEN_HOME="${MAVEN_HOME:-$HOME/.m2/wrapper/dists}"
MAVEN_VERSION=$(echo "$distributionUrl" | sed 's/.*apache-maven-\([0-9.]*\).*/\1/')
MAVEN_DIR="$MAVEN_HOME/apache-maven-$MAVEN_VERSION"

if [ ! -d "$MAVEN_DIR" ]; then
    echo "Downloading Maven $MAVEN_VERSION..."
    mkdir -p "$MAVEN_HOME"
    curl -sL "$distributionUrl" -o "$MAVEN_HOME/maven.zip"
    unzip -q "$MAVEN_HOME/maven.zip" -d "$MAVEN_HOME"
    rm "$MAVEN_HOME/maven.zip"
fi

exec "$MAVEN_DIR/bin/mvn" "$@"
