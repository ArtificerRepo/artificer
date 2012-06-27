# The Governance S-RAMP project

## Summary

This is the official Git repository for the Governance S-RAMP project, which is intended to be a part of the [JBoss Overlord](http://www.jboss.org/overlord).

The S-RAMP project is an implementation of the SOA Repository Artifact Model and Protocol ([S-RAMP]https://www.oasis-open.org/committees/s-ramp/)). 

## Get the code

The easiest way to get started with the code is to [create your own fork](http://help.github.com/forking/) of this repository, and then clone your fork:

	$ git clone git@github.com:<you>/s-ramp.git
	$ cd s-ramp
	$ git remote add upstream git://github.com/Governance/s-ramp.git
	
At any time, you can pull changes from the upstream and merge them onto your master:

	$ git checkout master               # switches to the 'master' branch
	$ git pull upstream master          # fetches all 'upstream' changes and merges 'upstream/master' onto your 'master' branch
	$ git push origin                   # pushes all the updates to your fork, which should be in-sync with 'upstream'

The general idea is to keep your 'master' branch in-sync with the 'upstream/master'.

## Building S-RAMP

We use Maven 3.x to build our software. The following command compiles all the code, installs the JARs into your local Maven repository, and runs all of the unit tests:

	$ mvn clean install

**NOTE** **Currently S-RAMP references version 3.0-SNAPSHOT of ModeShape, so you may need to build/install that first (or otherwise download and install 
it in your local Maven repo).  In the future this will not be a problem, once ModeShape 3 settles down and we can reference released versions.


## Contribute fixes and features

S-RAMP is open source, and we welcome anybody who wants to participate and contribute!

If you want to fix a bug or make any changes, please log an issue in the [S-RAMP JIRA](http://issues.jboss.org/browse/SRAMP) describing the bug
or new feature. Then we highly recommend making the changes on a topic branch named with the JIRA issue number. For example, this command creates
a branch for the SRAMP-1234 issue:

	$ git checkout -b sramp-1234

After you're happy with your changes and a full build (with unit tests) runs successfully, commit your changes on your topic branch
(using [really good comments](http://community.jboss.org/wiki/OverlordDevelopmentGuidelines#Commits)). Then it's time to check for
and pull any recent changes that were made in the official repository:

	$ git checkout master               # switches to the 'master' branch
	$ git pull upstream master          # fetches all 'upstream' changes and merges 'upstream/master' onto your 'master' branch
	$ git checkout sramp-1234           # switches to your topic branch
	$ git rebase master                 # reapplies your changes on top of the latest in master
	                                      (i.e., the latest from master will be the new base for your changes)

If the pull grabbed a lot of changes, you should rerun your build to make sure your changes are still good.
You can then either [create patches](http://progit.org/book/ch5-2.html) (one file per commit, saved in `~/sramp-1234`) with 

	$ git format-patch -M -o ~/sramp-1234 orgin/master

and upload them to the JIRA issue, or you can push your topic branch and its changes into your public fork repository

	$ git push origin sramp-1234         # pushes your topic branch into your public fork of S-RAMP

and [generate a pull-request](http://help.github.com/pull-requests/) for your changes. 

We prefer pull-requests, because we can review the proposed changes, comment on them,
discuss them with you, and likely merge the changes right into the official repository.