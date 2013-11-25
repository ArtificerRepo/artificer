
== Welcome ==
Welcome to the S-RAMP Distribution (${project.version}), thanks for 
downloading!


== Overview ==
This distribution comes with the following:

    1) bin/*.war - the WARs that make up the S-RAMP runtime, including
       (but not limited to) the server WAR and the Repository Browser UI WAR
    2) bin/s-ramp-shell-${project.version}.jar - the S-RAMP interactive shell.
    3) bin/s-ramp.* - shell and batch scripts to run the S-RAMP interactive 
       shell.
    4) demos - a number of demo maven projects to help you get started with 
       S-RAMP.
    5) src - all of the S-RAMP source code, in a number of "-sources" JARs.
    7) build.xml an Ant script that will install and configure S-RAMP into
       either Tomcat 7 or JBoss EAP 6.1.

== What do I do next? ==
This distribution works with version 6.1 of the JBoss Enterprise Application
Platform (JBoss EAP 6.1) *or* version 7 of Apache Tomcat.   When using EAP
you must first download the EAP ZIP distribution and unpack to your preferred
location.

    Download here:  http://www.jboss.org/jbossas/downloads

The installer will ask you a couple of questions and then do everything else 
for you! From the root of this distribution, simply run:

    ant

Once the installation completes, you can start EAP or Tomcat using the standard
startup scripts and then try out any of the following:

        - S-RAMP demos (included)
        - S-RAMP repository browser UI (http://localhost:8080/s-ramp-ui)
        - S-RAMP interactive shell (bin/s-ramp.sh)

You should be able to log in with the following credentials:

    Username: admin
    Password: <pw>
        