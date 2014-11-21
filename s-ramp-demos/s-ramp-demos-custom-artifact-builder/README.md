# S-RAMP Demos: Custom ArtifactBuilder

## Summary

This demo shows how to create and contribute a custom artifact builder to the S-RAMP
repository.  Custom artifact builders allow users to create their own logical artifact
models within the standard S-RAMP compliant repository.  It is expected that a custom
builder will extract pieces of a User Defined Artifact into a collection of derived
artifacts, also of type User Defined Artifact.

Note that this demo is really two pieces:  the project JAR contains both the custom
builder (which must be made available to the S-RAMP repository/server) and the 
client-side demo class (which runs the demo).

## How It Works

First you must build the project JAR and "deploy" it to the S-RAMP server.  To 
build the JAR, simply do the following:

    $ mvn clean package

Once the JAR is built, it must be made available to the S-RAMP repository server.  This
can be done either by adding the resulting project JAR to the global classpath of your
application server, or by pointing the S-RAMP repository to a directory containing the
project JAR.  To accomplish the former, please see the documentation for your 
application server.  For JBoss 7.1, the details can be found here:

    https://docs.jboss.org/author/display/AS7/Developer+Guide#DeveloperGuide-GlobalModules

Typically it's easier to create a local directory in which Custom Builder JARs can be
placed.  Once that is done, you must tell S-RAMP about the directory, by setting a 
system property 'sramp.extension.customDir'.  For example, I might do this:

    $ ~/bin/stopJBoss.sh
    $ mkdir ~/.s-ramp-builders
    $ cp target/*.jar ~/.s-ramp-builders
    $ ~/bin/runJBoss.sh -Dsramp.extension.customDir=/home/ewittman/.s-ramp-builders

You will need to create the directory, copy the project JAR into it, and then set the
appropriate -D system property when you start your application server.

To run the demo, you will need to supply valid user credentials.  You can do this
by passing the following properties using -D:

* sramp.auth.username - sets the BASIC auth username to use during the demo
* sramp.auth.password - sets the BASIC auth password to use during the demo

In short, it might look something like this:

	$ mvn -Pdemo -Dsramp.auth.username=admin -Dsramp.auth.password=MYPASSWORD clean test

*Note* - the demo expects/assumes the S-RAMP Atom API endpoint to be located at:

	http://localhost:8080/s-ramp-server

If you are running the S-RAMP repository on some other port or deployed in some other way
you can customize where the demo looks for the Atom API.  For example:

	$ mvn -Pdemo -Dsramp.endpoint=http://myhost:8081/s-ramp-server clean test

The demo should output some interesting information before completing successfully.  Please
take a look at the code found in the CustomArtifactBuilderDemo Java class for more information.

*Note* - you can also use the S-RAMP UI (browser) to take a look at the artifact that were
uploaded by this demo.  By default you can find the UI here:

	http://localhost:8080/s-ramp-ui/
