
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
    6) updates - bootstrapping configuration for JBoss and jBPM.
    7) build.xml/s-ramp-build.properties - an Ant script that will install 
       and configure S-RAMP in JBoss EAP 6.1.

== What do I do next? ==
This distribution works with version 6.1 of the JBoss Enterprise Application
Platform (JBoss EAP 6.1).  You must download EAP and point the S-RAMP installer
to the downloaded .zip.  You can accomplish the latter by simply copying the
downloaded EAP .zip file into the AS/EAP directory of this distribution, or you can 
modify the 's-ramp-build.properties' file to point to wherever you saved it.

    Download here:  http://www.jboss.org/jbossas/downloads

The installer should do everything else for you. From the root of this distribution, simply
run:

    ant

Once the installation completes, you can start JBoss (which you should find
in the 'target' directory) and try out any of the following:

        - S-RAMP demos (included)
        - S-RAMP repository browser UI (http://localhost:8080/s-ramp-ui)
        - S-RAMP interactive shell (bin/s-ramp.sh)

You should be able to log in with the following credentials:

    Username: admin
    Password: <pw>
        