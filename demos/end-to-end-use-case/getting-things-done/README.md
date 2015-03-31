# Artificer End-to-End Use Case: "Getting Things Done" (David Allen)

## Summary

"Getting Things Done", by David Allen, is a well known and well respected system for managing projects, tasks, and
reference information, for both personal and professional uses.  The system results in a large amount information
chunks, many of which are related and dependent.  Custom tags are also needed, as well as hierarchical metadata.

Sound familiar?  "Getting Things Done", meet Artificer...

This demo populates an Artificer repo with fake reference information, as well as a system for a "Getting Things Done"
style of project/task management.  It includes a custom OWL ontology with some ideas for hierarchical classifiers, in
addition to a helpful set of custom properties/tags.  The beauty of "Getting Things Done" is that it's purely a
skeleton system that can be molded for your own uses.  That's the same idea here.  This certainly isn't complete
or exhaustive, but simply demonstrates what's possible.

In this particular setup, we make heavy use of ontologies/classifiers for nearly every aspect of GTD.  They're easily
used and modified during runtime, in addition to having the benefit of being hierarchical in nature.  However, note
that this could also be implemented using generic properties (essentially making them 'tags') as an alternative.

Our GTD ontology (gettingthingsdone.owl.xml) consists of:

* Actions: Tasks to be acted upon.  Further broken down into classifiers based on the level of complexity:
    * Simple
    * Moderate
    * Complex
* Waiting: Actions to be acted upon, but currently held up by an external expectation (a call/email from someone, etc.).
* Someday: Actions that are on the back-burner (ie, the "tickler file").
* Context: Productivity increases when actions are grouped by context, rather than constantly shifting your focus.  Our's include:
	* Using The Computer
	* On The Phone
	* Out Running Errands

Note that GTD utilizes an 'inbox', prior to organizing and classifying actions.  You could certainly add that to the
ontology, however we skip it here.  The assumption is that other external sources (email, etc.) typical act as the
inboxes.

Running the demo (see below) will push in an example dataset, using the ontologies to classify everything in a GTD way.
Once its run, play around with the UI to see how it turned out.  Pay special attention to the Classifiers area within
the sidebar filters.  That will allow you to build queries, such as:

	/s-ramp[s-ramp:classifiedByAllOf(.,'http://artificer.jboss.org/gettingthingsdone.owl#ModerateAction','http://artificer.jboss.org/gettingthingsdone.owl#ComputerContext')]

IE, find all actions, moderate in complexity, that can be done while using the computer.  Or:

	/s-ramp[GTDProjectRel[@name='Bathroom Remodel']]

IE, find all actions associated with the 'Bathroom Remodel' project.

Are those queries verbose?  Absolutely.  But that's why the UI and other interfaces exist!

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

*Note* - you can also use the Artificer UI (browser) to take a look at the artifacts that were
uploaded by this demo.  By default you can find the UI here:

	http://localhost:8080/artificer-ui/
