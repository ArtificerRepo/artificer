# Artificer Demos: Simple Client

## Summary

This demo illustrates how easy it is to use the Artificer client to interact with the Artificer
repository (which it does via an Atom API).

## How It Works

To run the demo, you will need to supply valid user credentials.  You can do this
by passing the following properties using -D:

* artificer.auth.username - sets the BASIC auth username to use during the demo
* artificer.auth.password - sets the BASIC auth password to use during the demo

In short, it might look something like this:

	$ mvn -Pdemo -Dartificer.auth.username=admin -Dartificer.auth.password=MYPASSWORD clean test

*Note* - the demo expects the Artificer Atom API endpoint to be located at:

	http://localhost:8080/artificer-server

If you are running the Artificer repository on some other port or deployed in some other way
you can customize where the demo looks for the Atom API.  For example:

	$ mvn -Pdemo -Dartificer.endpoint=http://myhost:8081/artificer-server clean test

The demo should output some interesting information before completing successfully.  Please
take a look at the code found in the SimpleClientDemo Java class for more information.

*Note* - you can also use the Artificer UI (browser) to take a look at the artifact that were
uploaded by this demo.  By default you can find the UI here:

	http://localhost:8080/artificer-ui/
