# The Governance S-RAMP project

## Summary

This is the official git repository for Overlord S-RAMP, a component of the [Overlord](http://www.jboss.org/overlord) project.  Overlord S-RAMP provides an implementation of the SOA Repository Artifact Model and Protocol ([S-RAMP](https://www.oasis-open.org/committees/s-ramp/)).

## Get the code

The easiest way to get started with the code is to [create your own fork](http://help.github.com/forking/) of this repository, then clone it locally:

	$ git clone git@github.com:<you>/s-ramp.git
	$ cd s-ramp
	$ git remote add upstream git://github.com/Governance/s-ramp.git
	
At any time, you can pull changes from upstream and merge them onto your master:

	$ git checkout master               # switches to the 'master' branch
	$ git pull upstream master          # fetches all 'upstream' changes and merges 'upstream/master' onto your 'master' branch
	$ git push origin                   # pushes all the updates to your fork, which should be in-sync with 'upstream'

The general idea is to keep your 'master' branch in-sync with the 'upstream/master'.

## Building S-RAMP

We use Maven 3.x to build our software. The following command compiles all the code, installs the JARs into your local Maven repository, and runs all of the unit tests:

	$ mvn clean install
	
## Run S-RAMP for local development and testing

S-RAMP includes an "s-ramp-dev-server" module that runs the server and UI in an embedded Jetty app server.  It's great for local development, providing a fast and lightweight means for testing your bug fixes or new features.  Use the following command to start it up:

	$ mvn clean test -pl s-ramp-dev-server -P run

Then, test your changes through http://localhost:8080/s-ramp-ui/index.html or http://localhost:8080/s-ramp-server.  Note that the dev server uses a dummy, basic authentication facade.  If prompted by the UI or server, enter *any* username and password.

## Contribute fixes and features

S-RAMP is open source and we welcome anyone who wants to participate and contribute!

If you want to fix a bug or create a new feature, please log an issue in the [S-RAMP JIRA](http://issues.jboss.org/browse/SRAMP) describing the task. Then we highly recommend making the changes on a topic branch named with the JIRA issue number. For example, this command creates a branch for the SRAMP-1234 issue:

	$ git checkout -b artificer-1234

After you're happy with your changes and a full build (with unit tests) runs successfully, commit your changes on your topic branch.  Please ensure that the comment is descriptive and starts with the JIRA code (e.g. "SRAMP-1234 Added the feature to …..").  Then it's time to pull any recent changes that were made in the official repository.  The following fetches all 'upstream' changes and reapplies your changes on top (i.e., the latest from master will be the new base for your changes).

	$ git pull --rebase upstream master

If the pull grabbed a lot of changes, you should rerun your build to ensure your changes are still good.
You can then push your topic branch and its changes into your public fork repository

	$ git push origin artificer-1234         # pushes your topic branch into your public fork of S-RAMP

and [generate a pull-request](http://help.github.com/pull-requests/). 

We prefer pull-requests over patches because we can review the proposed changes, comment on them,
discuss them with you, and likely merge the changes right into the official repository.

Please also read the guidelines for contributors: https://github.com/Governance/overlord/wiki/Contributor-Guidelines
