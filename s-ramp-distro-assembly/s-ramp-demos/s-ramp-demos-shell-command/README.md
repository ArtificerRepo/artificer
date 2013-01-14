# S-RAMP Demos: Custom Shell Command

## Summary

This demo shows how to create and contribute a custom command for use in the included
S-RAMP interactive shell.  The S-RAMP interactive shell can be accessed in the distro
from the "bin" directory.  The shell provides a way to connect to an S-RAMP repository
and perform queries and updates.  Additionally, the shell is extensible, allowing new
commands to be contributed at runtime.

## How It Works

First you must build the project JAR, which will contain the implementation of the 
custom shell command.  This can be done simply using Maven:

    $ mvn clean package

Once the JAR is built, it must be made available to the S-RAMP interactive shell.  This
can be done by adding the resulting project JAR to the shell's classpath by copying it 
to the interactive shell's home directory:

    $ mkdir ~/.s-ramp
    $ mkdir ~/.s-ramp/commands
    $ cp target/*.jar ~/.s-ramp/commands/.

At this point you should use the scripts in the distribution's 'bin' directory to run 
the S-RAMP Interactive Shell.  Once running, you should see the new jvm:status command
in the help and you should be able to execute it:

    s-ramp> jvm:status
