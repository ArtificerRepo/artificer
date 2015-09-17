# Artificer Demos: Multi-Module WebApp

## Summary

This demo shows a multi-project application, its use of the Artificer Maven integration, and Artificer's ability to act as a full-featured artifact repository.  The Wagon is used for 2 purposes:

1.) Deploying the artifacts
2.) Downloading dependencies

## How It Works

*Note* - the demo expects the Artificer Atom API endpoint to be located at:

  http://localhost:8080/artificer-server

If you are running the Artificer repository on some other port or deployed in some other way
you can customize where the demo looks for the Atom API.  To do this you must update
the 'distributionManagement' element in the pom.xml file.

To run the demo, simply do the following:

  $ cd artifacts
  $ mvn -Pdemo clean deploy

Note that the artifacts jar is uploaded to Artificer via Maven.  Then:

  $ cd ../web
  $ mvn -Pdemo clean deploy

There are two things to note here.  First and foremost, the web project depends on the artifact project for its domain objects.  Note that the artifact was downloaded from Artificer, rather than your local repository (the artifact's pom.xml blocks local .m2 installation)!  Second, the war was also uploaded to Artificer.

# Note About Authentication

Whenever the Artificer Maven integration features are used, you will need to provide valid authentication credentials
in the Maven settings.xml file. However, the typical "username" and "password" values are not sufficient, since they are
ignored during artifact retrieval (ie, GET calls to the repo). Instead, you must explicitly define the BASIC
authentication header value. Unfortunately, this also means you have to manually Base64 encode the value. For example,
"Basic admin:artificer1!" becomes "Basic YWRtaW46YXJ0aWZpY2VyMSE=".

It’s a pain, but at least as of Maven 3.0.5, it’s the best option we could find (PLEASE correct us if we’re wrong!).

So, using that, your settings.xml file should contain two additional server entries in the servers section:

    <server>
	  <id>local-artificer-repo</id>
	  <configuration>
		<httpHeaders>
		  <property>
			<name>Authorization</name>
			<value>Basic YWRtaW46YXJ0aWZpY2VyMSE=</value>
		  </property>
		</httpHeaders>
	  </configuration>
	</server>
	<server>
	  <id>local-artificer-repo-snapshots</id>
	  <configuration>
		<httpHeaders>
		  <property>
			<name>Authorization</name>
			<value>Basic YWRtaW46YXJ0aWZpY2VyMSE=</value>
		  </property>
		</httpHeaders>
	  </configuration>
	</server>

# Results of the Deploy

After deploying, you should notice that
several files from within the JAR and WAR are automatically extracted and added as 
separate (but related) artifacts.  These in turn may cause additional derived artifacts
to be created!  The artifacts should include the JAR and WAR themselves, an XSD, and web.xml.

The artifacts all share a common maven groupId. During upload to the Artificer server a
Artificer ArtifactGroup extended object is created and relations are set between
this group and the artifacts. 

*Note* - you can also use the Artificer UI (browser) to take a look at the artifact that were
uploaded by this demo.  By default you can find the UI here:

  http://localhost:8080/artificer-ui/
