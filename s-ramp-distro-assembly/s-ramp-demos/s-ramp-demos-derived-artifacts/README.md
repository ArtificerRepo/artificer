# S-RAMP Demos: Classifications

## Summary

This demo shows how the S-RAMP repository will create derived artifacts for certain
types of uploaded files (Schemas, WSDLs, Policies, etc).  It will also show how to
query for artifacts based on derived content such as relationships and properties.

## How It Works

To run the demo, simply do the following:

	$ mvn -Pdemo clean test

*Note* - the demo expects the S-RAMP Atom API endpoint to be located at:

	http://localhost:8080/s-ramp-atom/s-ramp

If you are running the S-RAMP repository on some other port or deployed in some other way
you can customize where the demo looks for the Atom API.  For example:

	$ mvn -Pdemo -Dsramp.endpoint=http://myhost:8081/s-ramp-atom/s-ramp clean test

The demo should output some interesting information before completing successfully.  Please
take a look at the code found in the DerivedArtifactsDemo Java class for more information.

*Note* - you can also use the S-RAMP UI (browser) to take a look at the artifact that were
uploaded by this demo.  By default you can find the UI here:

	http://localhost:8080/s-ramp-ui/
