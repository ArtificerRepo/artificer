# S-RAMP Demos: Simple Client

## Summary

This demo illustrates how easy it is to use the S-RAMP client to interact with the S-RAMP
repository (which it does via an Atom API).

## How it works

To run the demo, simply do the following:

	$ mvn -Pdemo clean test

*Note* - the demo expects the S-RAMP Atom API endpoint to be located at:

	http://localhost:8080/s-ramp-atom/s-ramp

If you are running the S-RAMP repository on some other port or deployed in some other way
you can customize where the demo looks for the Atom API.  For example:

	$ mvn -Pdemo -Dsramp.endpoint=http://myhost:8081/s-ramp-atom/s-ramp

The demo should output some interesting information before completing successfully.  Please
take a look at the code found in the SimpleClientDemo Java class for more information.