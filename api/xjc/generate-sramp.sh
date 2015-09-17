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
CP=$PARENTDIR/target/artificer-api-1.1.0-SNAPSHOT.jar
########## CONFIG PARAMS ############################

echo Generating java classes in $JAVADIR using $CURDIR/jaxb-bindings.xml ...
xjc $XSDDIR/*.xsd -b $CURDIR/jaxb-bindings.xml -d $JAVADIR > tmpOutputFile

echo Commenting out serialVersionUID ...
index=0
while read line ; do
	MyArray[$index]="$line"
	index=$(($index+1))
        if [ "$index" -gt "2" ]; then
             fileList[$index-3]=$line
             sed -ie 's/private final static long serialVersionUID = 1L/\/\/private final static long serialVersionUID = 1L/g'  $JAVADIR/$line 
             cat $CURDIR/LICENSE.txt $JAVADIR/$line > $JAVADIR/${line}.license
             mv $JAVADIR/${line}.license $JAVADIR/$line
             line=$(echo $line|sed 's/.java//g')
             line=$(echo $line|sed 's/\//./g')
             classList[$index]=$line
        fi
done < "tmpOutputFile"

#printf "%s\n" "${classList[@]}"

rm tmpOutputFile

echo Building ...
mvn clean package -DskipTests=true

index=0
for className in "${classList[@]}"
do
   echo Adding serialVersionUID to $className
   serialVerId=`serialver -classpath $CP $className`
   serialVerId=$(echo $serialVerId|eval "sed 's/$className: //g'")
   file=${fileList[$index]}
   sed -ie 's/\/\/private final static long serialVersionUID = 1L/private final static long serialVersionUID = 1L/g'  $JAVADIR/$file
   eval "sed -ie 's/final static long serialVersionUID = 1L;/$serialVerId/g' $JAVADIR/$file"
   index=$(($index+1))
   rm $JAVADIR/${file}e
done
