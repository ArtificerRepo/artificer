#!/bin/sh
rm -rf jboss-fuse-6.1.0/data
mkdir -p jboss-fuse-6.1.0/data/tmp
rm -f jboss-fuse-6.1.0/instances/instance.properties
rm -rf system/.timestamps
rm -f jboss-fuse-6.1.0/lock
rm -rf jboss-fuse-6.1.0/etc/overlord-apps
rm -f jboss-fuse-6.1.0/etc/sramp.properties
rm -f jboss-fuse-6.1.0/etc/sramp-ui.properties
rm -f jboss-fuse-6.1.0/etc/sramp-modeshape.json
rm -f jboss-fuse-6.1.0/etc/overlord-saml.keystore

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
echo "Copying clean org.ops4j.pax.url.mvn.cfg from $DIR"
cp -f $DIR/org.ops4j.pax.url.mvn.cfg jboss-fuse-6.1.0/etc/

echo "Fuse is reset."