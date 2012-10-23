The ant script updates the configuration of the jbpm-console deployed to the jboss-as server installed
by the jBPM5 installer. Simply copy the build.properties-example to build.properties, 

cp build.properties-example build.properties

and update the jboss.home setting to point to jboss-as installed by the jbpm5 installer.

then run

ant

After running the script the jbpm console reads it's workflows from s-ramp, rather then drools-guvnor.
The files that changed are:

  1. build.xml
      removes the option to read workflows from the sample directory (-Djbpm.console.directory)
      
  2. jbpm-console.properties
      point jbpm to s-ramp-atom/brms on port 8800
  
  3. jbpm-gwt-shared-5.3.0-SRAMP.jar.
      not sure why our version works and the one that ships with jbpm does not want to connect to s-ramp. This is
      issue is on the list to discuss with the jBPM team.
