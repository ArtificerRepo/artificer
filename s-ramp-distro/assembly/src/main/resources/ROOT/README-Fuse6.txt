
== Welcome ==
Welcome to version ${project.version} of the Overlord S-RAMP Distribution, 
thanks for downloading!

== Running S-RAMP on Fuse 6.x ==
This version of S-RAMP supports running on the JBoss Fuse platform.  The 
procedure for doing this is different than running on other platforms we 
support.  All installation and configuration of S-RAMP on Fuse is done
through the Fuse/Karaf console.

== Quickstart ==
Follow these steps to get up and running with S-RAMP on Fuse:

1) Download Fuse 6.x:  http://www.jboss.org/download-manager/file/jboss-fuse-6.1.0.GA-full_zip.zip

   NOTE: this version of S-RAMP was tested with JBoss Fuse 6.1.0.GA - other 
         versions may not work or may require additional steps.

2) Unpack Fuse 6.x

3) Download a patch for Fuse 6.x - this patch fixes some problems with the
   Fuse package itself:  https://developer.jboss.org/servlet/JiveServlet/download/52622-22-125646/patches.zip

4) Unpack the patches.zip into the Fuse 6.x installation - it should prompt
   you to replace a few files.

5) Launch Fuse 6.x (typically via the fuse or fuse.bat script).  You should 
   probably also beef up your memory settings on Fuse.  For example:
   
   -Xms512M -Xmx1G -XX:PermSize=384m -XX:MaxPermSize=384m

6) Run the following commands on the Fuse console:

   features:addurl mvn:org.overlord.sramp/s-ramp-distro-fuse61/0.6.0-SNAPSHOT/xml/features
   features:install -v s-ramp-karaf-commands
   overlord:s-ramp:configure <ADMIN-USER-PASSWORD>
   features:install -v s-ramp

7) Go ahead and get started using S-RAMP!  You can find the UI here:

   http://localhost:8181/s-ramp-ui/
   Username:  admin
   Password:  <ADMIN-USER-PASSWORD>
