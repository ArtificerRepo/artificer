#!/bin/sh
echo ""
echo "######################################"
echo "  Releasing S-RAMP Distro (Community)"
echo "######################################"
echo ""
mvn -e --batch-mode -DignoreSnapshots=true -Prelease clean release:prepare release:perform

