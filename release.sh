#!/bin/sh
echo ""
echo "######################################"
echo "  Releasing S-RAMP (Community)"
echo "######################################"
echo ""
mvn -e --batch-mode clean release:prepare release:perform

