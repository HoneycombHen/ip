#!/bin/sh

set -e

APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
    JAVA_EXECUTABLE="$JAVA_HOME/bin/java"
else
    JAVA_EXECUTABLE="java"
fi

exec "$JAVA_EXECUTABLE" ${DEFAULT_JVM_OPTS:-} ${JAVA_OPTS:-} ${GRADLE_OPTS:-} \
    "-Dorg.gradle.appname=$(basename "$0")" \
    -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
