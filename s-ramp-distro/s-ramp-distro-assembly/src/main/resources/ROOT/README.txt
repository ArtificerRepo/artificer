
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
downloaded EAP .zip file into the root of this distribution, or you can 
modify the 's-ramp-build.properties' file to point to wherever you saved it.

    Download here:  http://www.jboss.org/jbossas/downloads

Overlord S-RAMP also uses the ModeShape project as its persistence store.  
So in addition to EAP you must download the ModeShape distribution for EAP
and follow the same procedure mentioned above (either copy the ModeShape ZIP
to the root of this distribution, or else modify s-ramp-build.properties.

    Download here:  http://www.jboss.org/modeshape/downloads/downloads3-2-0-final

(Note that we recommend version 3.2.0.Final of ModeShape at the time of this
writing.

Once these two additional dependencies have been downloaded, the installer
should do everything else for you.  From the root of this distribution, simply
run:

    ant

Once the installation completes, you can start JBoss (which you should find
in the 'target' directory) and try out any of the following:

        - S-RAMP demos (included)
        - S-RAMP repository browser UI (http://localhost:8080/s-ramp-ui)
        - S-RAMP interactive shell (bin/s-ramp.sh)

You should be able to log in with the following credentials:

    Username: gary
    Password: gary
        