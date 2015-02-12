#!/bin/sh
echo ""
echo "######################################"
echo "  Releasing Artificer"
echo "######################################"
echo ""
read -p "Release version: " RELEASE_VERSION
read -p "New development version: " DEV_VERSION
read -p "Full path to your private key: " KEYFILE

mvn versions:set -DnewVersion=$RELEASE_VERSION
find . -name '*.versionsBackup' -exec rm -f {} \;

# sanity check
mvn clean install -P generate-docs

git add .
git commit -m "Prepare for release $RELEASE_VERSION"
git push origin master

git tag -a -m "Tagging release $RELEASE_VERSION" artificer-$RELEASE_VERSION
git push origin artificer-$RELEASE_VERSION

mvn deploy -Pgenerate-docs,upload-docs -Dkeyfile=$KEYFILE
scp artificer-distro/assembly/target/artificer-$RELEASE_VERSION.zip artificer@filemgmt.jboss.org:/downloads_htdocs/artificer

mvn versions:set -DnewVersion=$DEV_VERSION
find . -name '*.versionsBackup' -exec rm -f {} \;
git add .
git commit -m "Update to next development version: $DEV_VERSION"
git push origin master
