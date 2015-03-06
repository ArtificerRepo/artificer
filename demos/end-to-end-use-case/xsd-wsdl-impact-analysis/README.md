# Artificer End-to-End Use Case: Impact Analysis with XSDs, WSDLs, and Large Development Teams

## Summary

In this demo, we pretend that we're a part of a large software development company, focused on provided
SOAP-based services.  Assume a common schema is used across the board.  Reuse is a good thing, right?

What if I want to update, replace, or create a new version of that schema?  What software and what teams will be
impacted?  More specifically, what will be impacted if I change a *specific element* of the schema?

This demo shows the processes and specific queries used in traversing the bi-directional relationship tree,
providing both coarse and fine grained impact analysis for your organization's artifacts.

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

The demo should output some interesting information before completing successfully.

*Note* - you can also use the Artificer UI (browser) to take a look at the artifactsthat were
uploaded by this demo.  By default you can find the UI here:

	http://localhost:8080/artificer-ui/
