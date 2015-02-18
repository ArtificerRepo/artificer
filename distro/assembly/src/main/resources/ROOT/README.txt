
== Welcome ==
Welcome to the Artificer Distribution (${project.version}), thanks for
downloading!


== Overview ==
This distribution comes with the following:

    1) bin/*.war - the WARs that make up the Artificer runtime, including
       (but not limited to) the server WAR and the Repository Browser UI WAR
    2) bin/artificer-shell-${project.version}.jar - the Artificer interactive shell.
    3) bin/artificer.* - shell and batch scripts to run the Artificer interactive
       shell.
    4) demos - a number of demo maven projects to help you get started with 
       Artificer.
    5) src - all of the Artificer source code, in a number of "-sources" JARs.
    6) build.xml - an Ant script that will install and configure Artificer into
       one of:
       B) JBoss Wildfly 8
       B) JBoss EAP 6

== What do I do next? ==
When installing into JBoss EAP you must first download the appropriate ZIP 
distribution and unpack to your preferred location.

    Download Wildfly or JBoss EAP:  http://www.jboss.org/jbossas/downloads

The installer will ask you a couple of questions and then do everything else 
for you! From the root of the distribution, simply run:

    ant

Once the installation completes, you can start Wildfly/EAP using the standard startup scripts, but make sure to
use the standalone-full profile.

    [JBOSS_HOME]/bin/standalone.sh -c standalone-full.xml

Then, try out any of the following:

        - Artificer demos (included)
        - Artificer repository browser UI (http://localhost:8080/artificer-ui)
        - Artificer interactive shell (bin/artificer.sh)

You should be able to log in with the following credentials:

    Username: admin
    Password: artificer1! (unless modified in Keycloak)

    
== Note on Memory Configuration ==
You will most likely need to increase the default JVM memory settings for
your application server.  The typical defaults are insufficient.  For
example, these settings are pretty good:

-Xms1G -Xmx1G -XX:PermSize=384m -XX:MaxPermSize=384m
