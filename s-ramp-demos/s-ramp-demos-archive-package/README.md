# S-RAMP Demos: Archive Package

## Summary

This demo illustrates how to use the S-RAMP archive package feature to upload multiple
artifacts to the repository at once (bundled in a ZIP formatted package file as defined
in the S-RAMP specification).

## How It Works

To run the demo, simply do the following:

	$ mvn -Pdemo clean test

*Note* - the demo expects the S-RAMP Atom API endpoint to be located at:

	http://localhost:8080/s-ramp-atom/s-ramp

If you are running the S-RAMP repository on some other port or deployed in some other way
you can customize where the demo looks for the Atom API.  For example:

	$ mvn -Pdemo -Dsramp.endpoint=http://myhost:8081/s-ramp-atom/s-ramp clean test

The demo should output some interesting information before completing successfully.  Please
take a look at the code found in the SimpleClientDemo Java class for more information.