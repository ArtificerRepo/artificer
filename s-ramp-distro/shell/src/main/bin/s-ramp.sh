#! /bin/sh

# -----------------------------------------------------------------------------
# Run script for the S-RAMP Interactive Shell
#
# Prerequisites:
#
#   Either java needs to be available on your PATH, or the JAVA_HOME
#   environment variable must be set and point at a valid Java installation.
#
# -----------------------------------------------------------------------------

BIN_DIR=$(dirname $0)

JAVA="$JAVA_HOME/bin/java"
if [ ! -r "$JAVA" ]; then
    JAVA=`which java`
fi

if [ ! -z "$JAVA" ] && [ -r "$JAVA" ]; then
  "$JAVA" -Xmx1024m -jar "$BIN_DIR/s-ramp-shell-${project.version}.jar" "$@"
else
  echo "Error: java needs to be available on your PATH, or the JAVA_HOME environment variable must be set and point at a valid Java installation."
  exit 1
fi
