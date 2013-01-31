# S-RAMP Demos: Classifications

## Summary

This demo shows a few examples of how classifications are handled in S-RAMP.  Classifications
are sort of like tags, exception they are hierarchical in nature and must be defined
first before they can be used.  Classifications are defined by ontologies, which are 
managed as separate resources in the S-RAMP repository.  For more on managing ontologies,
see the 's-ramp-demos-ontologies' demo.

## How It Works

To run the demo, simply do the following:

	$ mvn -Pdemo clean test

*Note* - the demo expects the S-RAMP Atom API endpoint to be located at:

	http://localhost:8080/s-ramp-server

If you are running the S-RAMP repository on some other port or deployed in some other way
you can customize where the demo looks for the Atom API.  For example:

	$ mvn -Pdemo -Dsramp.endpoint=http://myhost:8081/s-ramp-server clean test

The demo should output some interesting information before completing successfully.  Please
take a look at the code found in the ClassificationDemo Java class for more information.

*Note* - you can also use the S-RAMP UI (browser) to take a look at the artifact that were
uploaded by this demo.  By default you can find the UI here:

	http://localhost:8080/s-ramp-ui/
