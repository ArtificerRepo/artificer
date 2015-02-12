# S-RAMP Demos: Custom Shell Command

## Summary

This demo shows how to create and contribute a custom command for use in the included
S-RAMP interactive shell.  The S-RAMP interactive shell can be accessed in the full S-RAMP distro
from the "bin" directory.  The shell provides a way to connect to an S-RAMP repository
and perform queries and updates.  Additionally, the shell is extensible, allowing new
commands to be contributed at runtime.  This project contains a simple `JvmStatusCommand` that,
when executed, prints various JVM statistics.

## How It Works

First you must build the project JAR, which will contain the implementation of the 
custom shell command.  This can be done simply using Maven:

    $ cd s-ramp-demos/s-ramp-demos-shell-command
    $ mvn clean package

Once the demo project JAR is built, it must be made available to the S-RAMP interactive shell.  One way of doing
so is to copy the JAR to your local ~/.s-ramp directory.  The shell scans
this dir for jars and adds them to the set of available commands.

    $ mkdir ~/.s-ramp
    $ mkdir ~/.s-ramp/commands
    $ cp target/s-ramp-demos-shell-command-[version].jar ~/.s-ramp/commands/.

At this point you should use the scripts in the full S-RAMP distribution's 'bin' directory to run 
the S-RAMP Interactive Shell.  Once running, you should see the new jvm:status command
in the help and be able to execute it:

    $ cd [S-RAMP HOME]
    $ bin/s-ramp.sh
    s-ramp> jvm:status
