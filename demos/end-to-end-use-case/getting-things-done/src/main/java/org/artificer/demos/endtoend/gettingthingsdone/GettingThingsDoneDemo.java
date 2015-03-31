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
package org.artificer.demos.endtoend.gettingthingsdone;

import org.artificer.client.ArtificerAtomApiClient;
import org.artificer.common.ArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Relationship;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Target;

import java.io.InputStream;

/**
 * "Getting Things Done", by David Allen, is a well known and well respected system for managing projects, tasks, and
 * reference information, for both personal and professional uses.  The system results in a large amount information
 * chunks, many of which are related and dependent.  Custom tags are also needed, as well as hierarchical metadata.
 *
 * Sound familiar?  "Getting Things Done", meet Artificer...
 *
 * This demo populates an Artificer repo with fake reference information, as well as a system for a "Getting Things Done"
 * style of project/task management.  It includes a custom OWL ontology with some ideas for hierarchical classifiers, in
 * addition to a helpful set of custom properties/tags.  The beauty of "Getting Things Done" is that it's purely a
 * skeleton system that can be molded for your own uses.  That's the same idea here.  This certainly isn't complete
 * or exhaustive, but simply demonstrates what's possible.
 *
 * @author Brett Meyer
 */
public class GettingThingsDoneDemo {

	private static final String DEFAULT_ENDPOINT = "http://localhost:8080/artificer-server";
    private static final String DEFAULT_USER = "admin";
    private static final String DEFAULT_PASSWORD = "artificer1!";

    private static final String GTD_LIST_ITEM = "GTDListItem";
    private static final String GTD_PROJECT = "GTDProject";
    private static final String GTD_PROJECT_RELATIONSHIP = "GTDProjectRel";

	public static void main(String[] args) throws Exception {
		System.out.println("\n*** Running Demo ***\n");

        String endpoint = System.getProperty("artificer.endpoint");
        String username = System.getProperty("artificer.auth.username");
        String password = System.getProperty("artificer.auth.password");
        if (endpoint == null || endpoint.trim().length() == 0) {
            endpoint = DEFAULT_ENDPOINT;
        }
        if (username == null || username.trim().length() == 0) {
            username = DEFAULT_USER;
        }
        if (password == null || password.trim().length() == 0) {
            password = DEFAULT_PASSWORD;
        }
        System.out.println("Artificer Endpoint: " + endpoint);
        System.out.println("Artificer User: " + username);

        ArtificerAtomApiClient client = new ArtificerAtomApiClient(endpoint, username, password, true);

        System.out.println("\nUploading the ontology (gettingthingsdone.owl.xml)...");
        InputStream is = GettingThingsDoneDemo.class.getResourceAsStream("/gettingthingsdone.owl.xml");
        client.uploadOntology(is);
        is.close();

        System.out.println("\nCreating a few non-project actions...");
        createListItem(client, "finish filing taxes",
                "http://artificer.jboss.org/gettingthingsdone.owl#ComplexAction");
        createListItem(client, "call insurance company",
                "http://artificer.jboss.org/gettingthingsdone.owl#SimpleAction");
        BaseArtifactType shoppingList = createListItem(client, "write grocery shopping list",
                "http://artificer.jboss.org/gettingthingsdone.owl#ModerateAction");
        // include a description on the shopping list
        shoppingList.setDescription("item 1\nitem2\nitem3");
        client.updateArtifactMetaData(shoppingList);

        System.out.println("\nCreating project list...");
        BaseArtifactType demo = createProject(client, "Artificer Demo");
        BaseArtifactType bathroom = createProject(client, "Bathroom Remodel");
        BaseArtifactType bday = createProject(client, "Plan Wife's Birthday Party");

        System.out.println("\nCreating project actions...");
        createProjectListItem(client, "create slides", demo,
                "http://artificer.jboss.org/gettingthingsdone.owl#ModerateAction",
                "http://artificer.jboss.org/gettingthingsdone.owl#ComputerContext");
        createProjectListItem(client, "create Java project", demo,
                "http://artificer.jboss.org/gettingthingsdone.owl#ModerateAction",
                "http://artificer.jboss.org/gettingthingsdone.owl#ComputerContext");
        createProjectListItem(client, "buy paint", bathroom,
                "http://artificer.jboss.org/gettingthingsdone.owl#SimpleAction",
                "http://artificer.jboss.org/gettingthingsdone.owl#ErrandContext");
        BaseArtifactType shower = createProjectListItem(client, "install new shower", bathroom,
                "http://artificer.jboss.org/gettingthingsdone.owl#ComplexAction");
        createProjectListItem(client, "create invite list", bday,
                "http://artificer.jboss.org/gettingthingsdone.owl#ModerateAction");
        createProjectListItem(client, "figure out a good gift", bday,
                "http://artificer.jboss.org/gettingthingsdone.owl#ComplexAction");

//        System.out.println("\nAdding comments/notes on 'install new shower'...");
        // TODO

        System.out.println("\nCreating 'waiting' action ('book travel')...");
        BaseArtifactType travel = createProjectListItem(client, "book travel", demo,
                "http://artificer.jboss.org/gettingthingsdone.owl#ModerateAction",
                "http://artificer.jboss.org/gettingthingsdone.owl#ComputerContext",
                "http://artificer.jboss.org/gettingthingsdone.owl#Waiting");
        travel.setDescription("Waiting for email confirming the demo location.");
        client.updateArtifactMetaData(travel);

        System.out.println("\nCreating 'someday' actions...");
        createListItem(client, "complete RHCE certification",
                "http://artificer.jboss.org/gettingthingsdone.owl#Someday");
        createListItem(client, "learn how to fly fish",
                "http://artificer.jboss.org/gettingthingsdone.owl#Someday");

        // TODO: Incorporate the auditing UI, once developed, for 'completed' tasks that have been moved to the trash.

        System.out.println("\nCheck out the web UI (http://localhost:8080/artificer-ui/index.html) to see how it all turned out.");

        System.out.println("\n*** Demo Completed ***\n\n");
	}

    private static BaseArtifactType createListItem(ArtificerAtomApiClient client, String name, String... classifiers)
            throws Exception {
        BaseArtifactType action = createListItem(name, classifiers);
        return client.createArtifact(action);
    }

    private static BaseArtifactType createProjectListItem(ArtificerAtomApiClient client, String name,
            BaseArtifactType project, String... classifiers) throws Exception {
        BaseArtifactType action = createListItem(name, classifiers);
        Target target = new Target();
        target.setValue(project.getUuid());
        Relationship relationship = new Relationship();
        relationship.setRelationshipType(GTD_PROJECT_RELATIONSHIP);
        relationship.getRelationshipTarget().add(target);
        action.getRelationship().add(relationship);
        return client.createArtifact(action);
    }

    private static BaseArtifactType createListItem(String name, String... classifiers) {
        BaseArtifactType action = ArtifactType.ExtendedArtifactType(GTD_LIST_ITEM).newArtifactInstance();
        action.setName(name);
        for (String classifier : classifiers) {
            action.getClassifiedBy().add(classifier);
        }
        return action;
    }

    private static BaseArtifactType createProject(ArtificerAtomApiClient client, String name) throws Exception {
        BaseArtifactType action = ArtifactType.ExtendedArtifactType(GTD_PROJECT).newArtifactInstance();
        action.setName(name);
        return client.createArtifact(action);
    }
}
