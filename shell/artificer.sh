#!/bin/bash

SCRIPTDIR=$(dirname $0)
cd $SCRIPTDIR
mvn -Dexec.mainClass=org.artificer.shell.ArtificerShell exec:java
