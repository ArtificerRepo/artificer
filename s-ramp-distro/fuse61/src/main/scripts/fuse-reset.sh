#!/bin/sh
rm -rf jboss-fuse-6.1.0/data
mkdir -p jboss-fuse-6.1.0/data/tmp
rm -f jboss-fuse-6.1.0/instances/instance.properties
rm -rf system/.timestamps
rm -f jboss-fuse-6.1.0/lock

echo "Fuse is reset."