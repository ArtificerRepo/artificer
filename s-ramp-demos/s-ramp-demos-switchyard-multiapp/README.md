# S-RAMP Demos: Switchyard Deployment

## Summary

This demo shows how to integrate a standard Switchyard application build with 
the S-RAMP repository. This demo is a copy of the SwitchYard multiApp
https://github.com/jboss-switchyard/quickstarts/tree/master/demos/multiApp
In this demo we want to demo the use of artifactGrouping to create a grouping
in the S-RAMP repo.

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

This project uploads 4 artifacts

  * artifacts/target/OrderService.jar
  * order-service/target/switchyard-quickstart-demo-multi-order-service.jar
  * order-consumer/target/switchyard-quickstart-demo-multi-order-consumer.jar
  * web/target/switchyard-quickstart-demo-multi-web.war

which share a common maven groupId. During upload to the S-RAMP server a 
S-RAMP ArtifactGroup extended object is created and relations are set between
this group and these four artifacts. 

*Note* - you can also use the S-RAMP UI (browser) to take a look at the artifact that were
uploaded by this demo.  By default you can find the UI here:

  http://localhost:8080/s-ramp-ui/




****************************************************************************************************
For extra credit: Original README from the Switchyard project below:

# MultiApp Demo Quickstart

This quickstart provides an example of a multi-project application structure with SwitchYard.  The quickstart consists of the following pieces:

* artifacts : contains XSDs, WSDLs, and Java domain objects which are used by service providers and consumers across application projects
* order-service : provides two services - OrderService and InventoryService
* order-consumer : consumes OrderService through a SOAP/HTTP binding
* web : consumes InventoryService using it's Java service interface

The MultiApp quickstart can also be used to demonstrate design-time repository integration with SwitchYard.  Individual service 
artifacts in the artifacts project can be uploaded to a service repository (e.g. Guvnor) and exported 
as a service module for use within projects which consume the service.  Additional 
detail can be found in the SwitchYard Repository Integration wiki article.

Consult the README.md in each individual project for more info.

## Running the Example

1. Deploy JMS Queue

cp order-consumer/src/test/resources/switchyard-quickstart-demo-multi-order-consumer-hornetq-jms.xml ${AS}/standalone/deployments


2. Deploy each of the following to a SwitchYard AS7 runtime:
    * multiApp/artifacts/target/OrderService.jar (make sure to deploy this one first)
    * multiApp/order-service/target/switchyard-quickstart-demo-multi-order-service.jar
    * multiApp/order-consumer/target/switchyard-quickstart-demo-multi-order-consumer.jar
    * multiApp/web/target/switchyard-quickstart-demo-multi-web.war
    
3. Use one or both of the consuming application projects:
    * <b>Web</b>: Visit <http://localhost:8080/switchyard-quickstart-demo-multi-web>.
    * <b>JMS</b>: Use 'mvn exec:java' in the order-consumer project to submit a JMS order message via the OrderIntake service.

## Further Reading

1. [SwitchYard Repository Integration](https://community.jboss.org/wiki/SwitchYardRepositoryIntegration)
