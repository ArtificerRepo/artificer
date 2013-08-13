#! /bin/sh

# -----------------------------------------------------------------------------
# Run script for the S-RAMP Interactive Shell
#
# Environment Variable Prerequisites
#
#   JAVA_HOME       Must point at your Java Development Kit installation.
#
# -----------------------------------------------------------------------------

BIN_DIR=$(dirname $0)

if [ -r "$JAVA_HOME"/bin/java ]; then
  $JAVA_HOME/bin/java -Xmx1024m -jar $BIN_DIR/s-ramp-shell-${project.version}.jar $@
else
  echo "The JAVA_HOME environment variable is not defined correctly."
  echo "This environment variable is needed to run the S-RAMP shell."
  exit 1
fi
