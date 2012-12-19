
== Welcome ==
Welcome to the S-RAMP Distribution (${project.version}), thanks for 
downloading!


== Overview ==
This distribution comes with the following:

    1) bin/*.war - the WARs that make up the S-RAMP runtime, including
       (but not limited to) the server WAR and the Repository Browser UI
       WAR
    2) bin/s-ramp-shell-${project.version}.jar - the S-RAMP interactive shell 
       JAR.
    3) bin/s-ramp.* - shell and batch scripts to run the S-RAMP interactive 
       shell.
    4) bin/s-ramp-workflows-${project.version}.jar - a JAR that contains the
       built-in Governance workflows deployable to jBPM.
    5) bin/s-ramp-workitems-${project.version}.jar - a JAR containing code 
       needed by the built-in Governance workflows.
    6) demos - a number of demo maven projects to help you get started with 
       S-RAMP.
    7) src - all of the S-RAMP source code, in a number of "-sources" JARs.
    8) updates - bootstrapping configuration for JBoss and jBPM.
    9) build.xml/build.properties - an Ant script that will download, install, 
       and configure S-RAMP and jBPM in JBoss 7.1.


== What do I do next? ==
Everything you need to do should be included in the distribution and the included
installation Ant script (build.xml).  Here are some basic steps:

    1) ant install - this will do the following:
        - Download jBPM (takes a long time!)
        - Download RESTEasy
        - Install jBPM (inside a folder called "jbpm5")
        - Upgrade the RESTEasy service in JBoss 7.1
    2) ant configure - this will do the following:
        - Configure jBPM to point to S-RAMP instead of Drools-Guvnor
        - Deploy S-RAMP (server and browser UI) to JBoss
    3) ant start - this will fire up JBoss 7.1
    4) ant upload - this will seed Guvnor, S-RAMP, and jBPM with a basic Governance
       workflow
    5) Profit!  You're all set - from here you can try any of the following:
        - S-RAMP demos
        - S-RAMP governance workflows
        - S-RAMP repository browser UI (http://localhost:8080/s-ramp-ui)
        - S-RAMP interactive shell (see bin/s-ramp.sh)
