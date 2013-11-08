# S-RAMP Demos: Switchyard Deployment

## Summary

This demo shows how to integrate a standard Switchyard application build with 
the S-RAMP repository.

## How It Works

To run the demo, simply do the following:

	$ mvn -Pdemo clean deploy

*Note* - the demo expects the S-RAMP Atom API endpoint to be located at:

	http://localhost:8080/s-ramp-server

If you are running the S-RAMP repository on some other port or deployed in some other way
you can customize where the demo looks for the Atom API.  To do this you must update
the 'distributionManagement' element in the pom.xml file.

# Note About Authentication

Be aware that you must supply the maven build with credentials for your S-RAMP repository.  This
can be done by adding a section to your settings.xml file (or by providing a custom settings.xml
on the mvn command line using the '-s' argument).

For more information see:  http://maven.apache.org/settings.html

Your settings.xml file should contain two additional server entries in the servers section:

    <server>
      <id>local-sramp-repo</id>
      <username>admin</username>
      <password>PASSWORD</password>
    </server>
    <server>
      <id>local-sramp-repo-snapshots</id>
      <username>admin</username>
      <password>PASSWORD</password>
    </server>

# Results of the Deploy

The maven build will compile the Switchyard application into a JAR and then deploy the
resulting artifact into the S-RAMP repository.  In addition, you should notice that
several files from within the Switchyard JAR are automatically extracted and add as 
separate (but related) artifacts.  These in turn may cause additional derived artifacts
to be created!

*Note* - you can also use the S-RAMP UI (browser) to take a look at the artifact that were
uploaded by this demo.  By default you can find the UI here:

	http://localhost:8080/s-ramp-ui/
