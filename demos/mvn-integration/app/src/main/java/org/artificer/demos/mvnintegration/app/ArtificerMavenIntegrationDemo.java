/*
 * Copyright 2012 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.artificer.demos.mvnintegration.app;

import org.artificer.demos.mvnintegration.ObjectFactory;
import org.artificer.demos.mvnintegration.PersonType;

/**
 * A simle demo application that shows how this application can depend on
 * the s-ramp-demos-mvn-integration-artifacts JAXB library.  The demo
 * illustrates how that library can be uploaded to the S-RAMP repository
 * and then used as a dependency here.  This application doesn't do any
 * interesting logic - it just illustrates that code found in the
 * s-ramp-demos-mvn-integration-artifacts JAXB library can be used.
 *
 * @author eric.wittmann@redhat.com
 */
public class ArtificerMavenIntegrationDemo {

	/**
	 * Constructor.
	 */
	public ArtificerMavenIntegrationDemo() {
	}

	/**
	 * Runs the demo - this simply creates a PersonType and sets some values
	 * on it.
	 */
	public void runDemo() {
		ObjectFactory factory = new ObjectFactory();
		PersonType person = factory.createPersonType();
		person.setFirstName("Charles");
		person.setMiddleInitial("Francis");
		person.setLastName("Xavier");

		String name = person.getFirstName() + " " + person.getLastName();
		System.out.println("Professor X's name is: " + name);
	}

	/**
	 * Simple main.
	 * @param args
	 */
	public static void main(String [] args) {
		ArtificerMavenIntegrationDemo demo = new ArtificerMavenIntegrationDemo();
		demo.runDemo();
	}

}
