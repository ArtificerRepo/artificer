#!/bin/bash
# This bash script is meant to generate JAVA classes from xsds that are Serializable. With the help of 
# jaxb-bindings.xml file xjc creates the classes, with a defaulted value of serialVersionUID = 1L.
# The line containing the serialVersionUID is then commented out, and the classes are compiled
# using maven. The jar produced by maven is then put on the classpath when the serialVer tool is
# run. Finally we update the each java class with the unique serialVersionUID.

# This script assumes that the parent dir contains the pom.xml to build the jar.

CURDIR=`pwd`
cd ..
PARENTDIR=`pwd`

########## CONFIG PARAMS ############################
# Directory containing the xsds
XSDDIR=$PARENTDIR/src/main/resources/s-ramp
# Directory where java classes will be generated
JAVADIR=$PARENTDIR/src/main/java
# The classpath with the compiled version of the generated java classes
CP=$PARENTDIR/target/s-ramp-0.0.1-SNAPSHOT.jar
########## CONFIG PARAMS ############################

echo Generating java classes in $JAVADIR using $CURDIR/jaxb-bindings.xml ...
xjc $XSDDIR/*.xsd -b $CURDIR/jaxb-bindings.xml -d $JAVADIR 
