#!/bin/sh
echo ""
echo "######################################"
echo "  Releasing Overlord S-RAMP"
echo "######################################"
echo ""
read -p "Release Version: " RELEASE_VERSION
read -p "New Development Version: " DEV_VERSION

mvn versions:set -DnewVersion=$RELEASE_VERSION
find . -name '*.versionsBackup' -exec rm -f {} \;
git add .
git commit -m "Prepare for release $RELEASE_VERSION"
git push origin master

mvn clean install

git tag -a -m "Tagging release $RELEASE_VERSION" s-ramp-$RELEASE_VERSION
git push origin s-ramp-$RELEASE_VERSION

mvn deploy

mvn versions:set -DnewVersion=$DEV_VERSION
find . -name '*.versionsBackup' -exec rm -f {} \;
git add .
git commit -m "Update to next development version: $DEV_VERSION"
git push origin master

