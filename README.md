# Artificer: Software Artifact, Metadata, and Information Repository

## Overview

All individuals, teams, and organizations tend to have a tangled mess of "stuff".  That bucket can include bits of
information, logical metadata, and physical files.  Those "artifacts" are almost never isolated in nature.  They're all
connected and inter-dependent, but the relationships can be difficult to understand.

In the software development world, this is an especially important problem to solve.  The development process
often spews out a huge amount of artifacts, needed for future analysis and actions.  Without the ability to
analyze how the information, artifacts, and content within the artifacts are connected, development
processes become difficult, at best, or nearly impossible, at worst.  Further, it's not enough to simply know how the
bits are related.  How do you correlate the artifacts/metadata with, for example, service endpoints,
the responsible teams/individuals, and change histories?

Both the publisher and the consumer need help!

Artificer is 100% open source -- contributions are welcome!

## Summary

In steps Artificer.  Artificer is a software artifact, logical metadata, and information repository.  It consists of a
common data model, multiple interfaces, useful tools, and extensibility.  In less words?
It's a powerful platform that *untangles everything*.

## Links

- [Website](http://artificer.jboss.org/)
- [Downloads](http://artificer.jboss.org/downloads.html)
- [Docs](http://docs.jboss.org/artificer)
- [How to Contribute](http://artificer.jboss.org/contribute.html)
- [Feature/Bug Tracker](https://issues.jboss.org/browse/ARTIF)
- [Roadmap](http://artificer.jboss.org/roadmap.html)
- [Chat](http://artificer.jboss.org/chat.html)
- [User Forum](https://developer.jboss.org/en/artificer)
- [Developer Forum](https://developer.jboss.org/en/artificer/dev)
- [@ArtificerRepo](https://twitter.com/ArtificerRepo)
- [Blog](https://developer.jboss.org/en/artificer/blog)

## Get the code

The easiest way to get started with the code is to [create your own fork](http://help.github.com/forking/) of this repository, then clone it locally:

	$ git clone https://github.com/<you>/artificer.git
	$ cd artificer
	$ git remote add upstream https://github.com/ArtificerRepo/artificer.git
	
At any time, you can pull changes from upstream and merge them onto your master:

	$ git checkout master               # switches to the 'master' branch
	$ git pull upstream master          # fetches all 'upstream' changes and merges 'upstream/master' onto your 'master' branch
	$ git push origin                   # pushes all the updates to your fork, which should be in-sync with 'upstream'

The general idea is to keep your 'master' branch in-sync with the 'upstream/master'.

## Building Artificer

We use Maven 3.x to build our software. The following command compiles all the code, installs the JARs into your local Maven repository, and runs all of the unit tests:

	$ mvn clean install
	
## Run Artificer for local development and testing

Artificer includes an "dev-server" module that runs the server and UI in an embedded Jetty app server.  It's great for local development, providing a fast and lightweight means for testing your bug fixes or new features.  Use the following command to start it up:

	$ mvn clean test -pl dev-server -P run

Then, test your changes through http://localhost:8080/artificer-ui/index.html or http://localhost:8080/artificer-server.  Note that the dev server uses a dummy, basic authentication facade.  If prompted by the UI or server, enter *any* username and password.

## Contribute fixes and features

Artificer is open source and we welcome anyone who wants to participate and contribute!

If you want to fix a bug or create a new feature, please log an issue in the [Artificer JIRA](http://issues.jboss.org/browse/ARTIF) describing the task. Then we highly recommend making the changes on a topic branch named with the JIRA issue number. For example, this command creates a branch for the ARTIF-1234 issue:

	$ git checkout -b artificer-1234

After you're happy with your changes and a full build (with unit tests) runs successfully, commit your changes on your topic branch.  Please ensure that the comment is descriptive and starts with the JIRA code (e.g. "ARTIF-1234 Added the feature to …..").  Then it's time to pull any recent changes that were made in the official repository.  The following fetches all 'upstream' changes and reapplies your changes on top (i.e., the latest from master will be the new base for your changes).

	$ git pull --rebase upstream master

If the pull grabbed a lot of changes, you should rerun your build to ensure your changes are still good.
You can then push your topic branch and its changes into your public fork repository

	$ git push origin artificer-1234         # pushes your topic branch into your public fork of Artificer

and [generate a pull-request](http://help.github.com/pull-requests/). 

We prefer pull-requests over patches because we can review the proposed changes, comment on them,
discuss them with you, and likely merge the changes right into the official repository.
