# S-RAMP Demos: Multi-Module WebApp

## Summary

This demo shows a multi-project application, its use of the S-RAMP Maven integration, and S-RAMP's ability to act as a full-featured artifact repository.  The Wagon is used for 2 purposes:

1.) Deploying the artifacts
2.) Downloading dependencies

## How It Works

*Note* - the demo expects the S-RAMP Atom API endpoint to be located at:

  http://localhost:8080/s-ramp-server

If you are running the S-RAMP repository on some other port or deployed in some other way
you can customize where the demo looks for the Atom API.  To do this you must update
the 'distributionManagement' element in the pom.xml file.

To run the demo, simply do the following:

  $ cd artifacts
  $ mvn -Pdemo clean deploy

Note that the artifacts jar is uploaded to S-RAMP via Maven.  Then:

  $ cd ../web
  $ mvn -Pdemo clean deploy

There are two things to note here.  First and foremost, the web project depends on the artifact project for its domain objects.  Note that the artifact was downloaded from S-RAMP, rather than your local repository (the artifact's pom.xml blocks local .m2 installation)!  Second, the war was also uploaded to S-RAMP.

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

After deploying, you should notice that
several files from within the JAR and WAR are automatically extracted and added as 
separate (but related) artifacts.  These in turn may cause additional derived artifacts
to be created!  The artifacts should include the JAR and WAR themselves, an XSD, and web.xml.

The artifacts all share a common maven groupId. During upload to the S-RAMP server a 
S-RAMP ArtifactGroup extended object is created and relations are set between
this group and the artifacts. 

*Note* - you can also use the S-RAMP UI (browser) to take a look at the artifact that were
uploaded by this demo.  By default you can find the UI here:

  http://localhost:8080/s-ramp-ui/
