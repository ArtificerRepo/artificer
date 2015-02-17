# Artificer Demos: Custom Shell Command

## Summary

This demo shows how to create and contribute a custom command for use in the included
Artificer interactive shell.  The Artificer interactive shell can be accessed in the full Artificer distro
from the "bin" directory.  The shell provides a way to connect to an Artificer repository
and perform queries and updates.  Additionally, the shell is extensible, allowing new
commands to be contributed at runtime.  This project contains a simple `JvmStatusCommand` that,
when executed, prints various JVM statistics.

## How It Works

First you must build the project JAR, which will contain the implementation of the 
custom shell command.  This can be done simply using Maven:

    $ cd demos/shell-command
    $ mvn clean package

Once the demo project JAR is built, it must be made available to the Artificer interactive shell.  One way of doing
so is to copy the JAR to your local ~/.artificer directory.  The shell scans
this dir for jars and adds them to the set of available commands.

    $ mkdir ~/.artificer
    $ mkdir ~/.artificer/commands
    $ cp target/artificer-demos-shell-command-[version].jar ~/.artificer/commands/.

At this point you should use the scripts in the full Artificer distribution's 'bin' directory to run
the Artificer Interactive Shell.  Once running, you should see the new jvm:status command
in the help and be able to execute it:

    $ cd [ARTIFICER HOME]
    $ bin/artificer.sh
    artificer> jvm:status
