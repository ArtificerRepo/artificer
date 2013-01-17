# S-RAMP Demos: Maven Integration

## Summary

This demo shows how S-RAMP can be integrated with a simple Maven build.  This demo contains two
projects:

* s-ramp-demos-mvn-integration-artifacts: a very simple schema-first JAXB project, containing two XSD files from which Java code is generated via a Maven JAXB plugin
* s-ramp-demos-mvn-integration-app: a simple Java project that depends on the generated Java code from the above 'artifacts' project

The goal is to show that the 'artifacts' project can be built and deployed directly to the S-RAMP 
repository using just Maven.  Once the 'artifacts' project is deployed to the S-RAMP repository, the
'app' project can be successfully built (it will download the 'artifacts' dependency from the S-RAMP
repo).

## How It Works

# Deploying the 'artifacts' JAR to S-RAMP

To get this demo working you must be running the S-RAMP repository (see documentation for the S-RAMP
project to learn how to run the S-RAMP repository).

The first step is to build and deploy the 'artifacts' project to S-RAMP using Maven:

	$ cd s-ramp-demos-mvn-integration-artifacts
	$ mvn -Pdemo clean deploy

That will enable the 'demo' profile, which will configure the Maven **distributionManagement** to
point to a local S-RAMP repository (http://localhost:8080/s-ramp-server/).  Therefore you need to
be running S-RAMP on port 8080 and deployed as the 's-ramp-atom' context.

The build should complete successfully.

At this point there should be a number of artifacts stored in the S-RAMP repository.  You can verify
that by deploying the "s-ramp-ui" project and then navigating to http://localhost:8080/s-ramp-ui (or
the appropriate URL for you).

You should see the following artifacts in the S-RAMP repository:

* s-ramp-demos-mvn-integration-artifacts-VERSION.jar
* s-ramp-demos-mvn-integration-artifacts-VERSION.pom
* address.xsd
* person.xsd

It is likely obvious why those first two artifacts are in the repository.  But in addition, the
address.xsd and person.xsd are present because the Maven->S-RAMP integration is configured to
automatically "expand" the deployed Maven artifact (JAR, WAR, EAR).  This means that any 
interesting S-RAMP files found in the Maven artifact being deployed will also get added to the
S-RAMP repository (with appropriate meta data).  This feature provides some visibility into the
Maven artifact from within the S-RAMP repository UI.

# Depending on an artifact deployed to S-RAMP

Now that the 'artifacts' JAR is safely tucked away in the S-RAMP repository, the 'app' project 
(which depends on the 'artifacts' JAR) can be built.  But first, make sure you don't already 
have the 'artifacts' JAR installed in your local .m2 repository!

Delete the 'artifacts' JAR from your local .m2 repository:

	$ rm -rf ~/.m2/repository/org/overlord/sramp/demos/s-ramp-demos-mvn-integration-artifacts

Ok great, now go ahead and build the 'app':

	$ cd s-ramp-demos-mvn-integration-app
	$ mvn -Pdemo clean package

The build should complete successfully.  You will notice that the 'artifacts' JAR was downloaded
during the build and stored in your local .m2 directory.  Check it out and be amazed!
