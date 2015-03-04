# Artificer Demos: Maven Integration

## Summary

This demo shows how Artificer can be integrated with a simple Maven build.  This demo contains two
projects:

* artifacts: a very simple schema-first JAXB project, containing two XSD files from which Java code is generated via a Maven JAXB plugin
* app: a simple Java project that depends on the generated Java code from the above 'artifacts' project

The goal is to show that the 'artifacts' project can be built and deployed directly to the Artificer
repository using just Maven.  Once the 'artifacts' project is deployed to the Artificer repository, the
'app' project can be successfully built (it will download the 'artifacts' dependency from the Artificer
repo).

## How It Works

# Deploying the 'artifacts' JAR to Artificer

To get this demo working you must be running the Artificer repository (see documentation for the Artificer
project to learn how to run the Artificer repository).

The first step is to build and deploy the 'artifacts' project to Artificer using Maven:

	$ cd artifacts
	$ mvn -Pdemo clean deploy

# Note About Authentication

Be aware that you must supply the maven build with credentials for your Artificer repository.  This
can be done by adding a section to your settings.xml file (or by providing a custom settings.xml
on the mvn command line using the '-s' argument).

For more information see:  http://maven.apache.org/settings.html

Your settings.xml file should contain two additional server entries in the servers section:

    <server>
      <id>local-artificer-repo</id>
      <username>admin</username>
      <password>PASSWORD</password>
    </server>
    <server>
      <id>local-artificer-repo-snapshots</id>
      <username>admin</username>
      <password>PASSWORD</password>
    </server>

# Results of the Deploy

That will enable the 'demo' profile, which will configure the Maven **distributionManagement** to
point to a local Artificer repository (http://localhost:8080/s-ramp-server/).  Therefore you need to
be running Artificer on port 8080 and deployed as the 's-ramp-atom' context.

The build should complete successfully.

At this point there should be a number of artifacts stored in the Artificer repository.  You can verify
that by deploying the "artificer-ui" project and then navigating to http://localhost:8080/s-ramp-ui (or
the appropriate URL for you).

You should see the following artifacts in the Artificer repository:

* artificer-demos-mvn-integration-artifacts-VERSION.jar
* artificer-demos-mvn-integration-artifacts-VERSION.pom
* address.xsd
* person.xsd

It is likely obvious why those first two artifacts are in the repository.  But in addition, the
address.xsd and person.xsd are present because the Maven->Artificer integration is configured to
automatically "expand" the deployed Maven artifact (JAR, WAR, EAR).  This means that any 
interesting Artificer files found in the Maven artifact being deployed will also get added to the
Artificer repository (with appropriate meta data).  This feature provides some visibility into the
Maven artifact from within the Artificer repository UI.

# Depending on an artifact deployed to Artificer

Now that the 'artifacts' JAR is safely tucked away in the Artificer repository, the 'app' project
(which depends on the 'artifacts' JAR) can be built.

Ok great, now go ahead and build the 'app':

	$ cd app
	$ mvn -Pdemo clean package

The build should complete successfully.  You will notice that the 'artifacts' JAR was downloaded
during the build and stored in your local .m2 directory.  Check it out and be amazed!
