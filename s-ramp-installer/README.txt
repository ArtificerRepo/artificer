
1. Installing S-RAMP to JBoss EAP-6.1

Download the jbosseap-6.1.0.zip into the s-ramp-installer directory. Then in the same
directory issue

mvn clean package -Peap61

This will create a fully configured application server in the target/jbosseap-6.1 directory.
You can start the server by going into the targat/jboss-eap-6.1/bin directory and by issueing

./standalone.sh


2. Installing S-RAMP to Tomcat-7.x

Not yet implementented.