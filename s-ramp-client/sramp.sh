#!/bin/bash

# Enter the interactive shell
if [ "$1" == "-i" ]
then
    java -cp target/s-ramp-client-0.0.3-SNAPSHOT.jar:$(echo target/lib/*.jar | tr ' ' ':') org.overlord.sramp.client.shell.SrampShell
    exit 0
fi

echo "******************************************************************************************************"
echo "*                     _________         __________    _____      _____ __________                    *"
echo "*                    /   _____/         \______   \  /  _  \    /     \\\______   \                   *"
echo "*                    \_____  \   ______  |       _/ /  /_\  \  /  \ /  \|     ___/                   *"
echo "*                    /        \ /_____/  |    |   \/    |    \/    Y    \    |                       *"
echo "*                   /_______  /          |____|_  /\____|__  /\____|__  /____|                       *"
echo "*                           \/                  \/         \/         \/                             *"
echo "* JBoss S-RAMP Kurt Stam and Eric Wittmann, Licensed under the Apache License, V2.0, Copyrights 2012 *"
echo "******************************************************************************************************"

if [ $# -lt 1 ]
then
    echo "Not enough arguments were supplied. Usage:"
    echo "sramp.sh -i  ....runs the S-RAMP interactive shell"
    echo "sramp.sh brms [pkg_in_brms] [tag] [baseUrl] .....obtains 'pkg_in_brms' from brms/drools and uploads 
                                                           to s-ramp"
    exit 0
fi

if [ "$1" == "brms" ]
then
    echo "Uploading brms package '$2' to S-RAMP..."
    java -cp target/s-ramp-client-0.0.3-SNAPSHOT.jar:$(echo target/lib/*.jar | tr ' ' ':') org.overlord.sramp.client.brms.BrmsPkgToSramp $2 $3 $4
    exit 0
fi
