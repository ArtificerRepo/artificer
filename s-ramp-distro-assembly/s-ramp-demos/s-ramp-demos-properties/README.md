# S-RAMP Demos: Archive Package

## Summary

This demo shows how to manage properties of an S-RAMP artifact.  It also demonstrates
how to query for artifacts in the repository by their property values.

## How It Works

To run the demo, simply do the following:

	$ mvn -Pdemo clean test

*Note* - the demo expects the S-RAMP Atom API endpoint to be located at:

	http://localhost:8080/s-ramp-server

If you are running the S-RAMP repository on some other port or deployed in some other way
you can customize where the demo looks for the Atom API.  For example:

	$ mvn -Pdemo -Dsramp.endpoint=http://myhost:8081/s-ramp-atom/s-ramp clean test

The demo should output some interesting information before completing successfully.  Please
take a look at the code found in the PropertyDemo Java class for more information.

*Note* - you can also use the S-RAMP UI (browser) to take a look at the artifact that were
uploaded by this demo.  By default you can find the UI here:

	http://localhost:8080/s-ramp-ui/