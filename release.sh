#!/bin/sh
echo ""
echo "######################################"
echo "  Releasing Overlord S-RAMP"
echo "######################################"
echo ""
read -p "Release version: " RELEASE_VERSION
read -p "New development version: " DEV_VERSION
read -p "Full path to your private key: " KEYFILE

mvn versions:set -DnewVersion=$RELEASE_VERSION
find . -name '*.versionsBackup' -exec rm -f {} \;
git add .
git commit -m "Prepare for release $RELEASE_VERSION"
git push origin master

mvn clean install

git tag -a -m "Tagging release $RELEASE_VERSION" s-ramp-$RELEASE_VERSION
git push origin s-ramp-$RELEASE_VERSION

mvn deploy -P generate-docs -Dkeyfile=$KEYFILE

mvn versions:set -DnewVersion=$DEV_VERSION
find . -name '*.versionsBackup' -exec rm -f {} \;
git add .
git commit -m "Update to next development version: $DEV_VERSION"
git push origin master

