#!/bin/bash

SCRIPTDIR=$(dirname $0)
cd $SCRIPTDIR

mvn -Dexec.mainClass=org.overlord.sramp.client.shell.SrampShell exec:java
