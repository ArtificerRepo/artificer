
1. Installing S-RAMP to JBoss EAP-6.1
-------------------------------------
Download the jbosseap-6.1.0.zip into the s-ramp-installer directory. Then in the same
directory issue:

  mvn clean install -Peap61

This will create a fully configured application server in the target/jbosseap-6.1 directory.
You can start the server by going into the target/jboss-eap-6.1 directory and by issuing:

  ./bin/standalone.sh


2. Installing S-RAMP to JBoss AS 7.1.1.Final
--------------------------------------------
Download the jboss-as-7.1.1.Final.zip into the s-ramp-installer directory. Then in the same
directory issue:

  mvn clean install -Pjboss7

This will create a fully configured application server in the target/jboss-as-7.1.1.Final 
directory.  You can start the server by going into the target/jboss-as-7.1.1.Final directory 
and then issuing:

  ./bin/standalone.sh



3. Installing S-RAMP to Tomcat-7.x
----------------------------------
In the s-ramp-installer directory issue

  mvn clean install -Ptest-install-tomcat7
  
This will download and configure a tomcat server in the target/apache-tomcat-<version>
directory. You can start the server by going into the target/apache-tomcat-<version> directory
and then issuing

  ./bin/startup.sh

4. Installing S-RAMP to Jetty-8.x
----------------------------------
In the s-ramp-installer directory issue

  mvn clean install -Ptest-install-jetty8
  
This will download and configure a jetty server in the target/jetty-distribution-<version>
directory. You can start the server by going into the target/jetty-distribution-<version> directory
and then issuing

  ./bin/jetty.sh start
  
  