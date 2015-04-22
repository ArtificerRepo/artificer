/*
 * Copyright 2011 JBoss Inc
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
package org.artificer.repository.test;

import org.artificer.common.ontology.ArtificerOntology;
import org.artificer.common.ontology.ArtificerOntologyClass;
import org.junit.Assert;
import org.junit.Test;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Unit test for JCR persistence of S-RAMP ontologies.
 *
 * @author eric.wittmann@redhat.com
 */
public class OntologyPersistenceTest extends AbstractNoAuditingPersistenceTest {

    @Test
    public void testPersistOntology_Empty() throws Exception {
    	ArtificerOntology ontology = new ArtificerOntology();
    	ontology.setBase("urn:example.org/test1");
    	ontology.setLabel("Test Ontology #1");
    	ontology.setComment("This is my first test ontology.");
    	String uuid = persistenceManager.persistOntology(ontology).getUuid();
    	Assert.assertNotNull(uuid);

    	ArtificerOntology actual = persistenceManager.getOntology(uuid);
    	Assert.assertEquals(ontology.getUuid(), actual.getUuid());
    	Assert.assertEquals(ontology.getBase(), actual.getBase());
    	Assert.assertEquals(ontology.getLabel(), actual.getLabel());
    	Assert.assertEquals(ontology.getComment(), actual.getComment());
    	Assert.assertEquals(ontology.getId(), actual.getId());
    }

    @Test
    public void testPersistOntology_Full() throws Exception {
    	ArtificerOntology ontology = new ArtificerOntology();
    	ontology.setBase("urn:example.org/test2");
    	ontology.setLabel("Test Ontology #2");
    	ontology.setComment("This is my second test ontology.");

    	ArtificerOntologyClass world = createClass(ontology, null, "World", "World", "The entire world");
    	ArtificerOntologyClass asia = createClass(ontology, world, "Asia", "Asia", null);
    	ArtificerOntologyClass europe = createClass(ontology, world, "Europe", "Europe", "Two world wars");
    	ArtificerOntologyClass japan = createClass(ontology, asia, "Japan", "Japan", "Samurai *and* ninja?  Not fair.");
    	ArtificerOntologyClass china = createClass(ontology, asia, "China", "China", "Gunpowder!");
    	ArtificerOntologyClass uk = createClass(ontology, europe, "UnitedKingdom", "United Kingdom", "The food could be better");
    	ArtificerOntologyClass germany = createClass(ontology, europe, "Germany", "Germany", "The fatherland");

    	ontology.getRootClasses().add(world);

    	world.getChildren().add(asia);
    	world.getChildren().add(europe);
    	asia.getChildren().add(japan);
    	asia.getChildren().add(china);
    	europe.getChildren().add(uk);
    	europe.getChildren().add(germany);

    	String uuid = persistenceManager.persistOntology(ontology).getUuid();
    	Assert.assertNotNull(uuid);

    	ArtificerOntology actual = persistenceManager.getOntology(uuid);
    	Assert.assertEquals(ontology.getUuid(), actual.getUuid());
    	Assert.assertEquals(ontology.getBase(), actual.getBase());
    	Assert.assertEquals(ontology.getLabel(), actual.getLabel());
    	Assert.assertEquals(ontology.getComment(), actual.getComment());
    	Assert.assertEquals(ontology.getId(), actual.getId());
    	Assert.assertEquals(1, actual.getRootClasses().size());

    	ArtificerOntologyClass actualWorld = actual.getRootClasses().get(0);
    	Assert.assertEquals(world.getUri(), actualWorld.getUri());
    	Assert.assertEquals(world.getLabel(), actualWorld.getLabel());
    	Assert.assertEquals(world.getComment(), actualWorld.getComment());
    	Assert.assertEquals(world.getId(), actualWorld.getId());
    	Assert.assertNull(actualWorld.getParent());
    	Assert.assertEquals(2, actualWorld.getChildren().size());

    	ArtificerOntologyClass actualAsia = actualWorld.getChildren().get(0);
    	Assert.assertEquals(asia.getUri(), actualAsia.getUri());
    	Assert.assertEquals(asia.getLabel(), actualAsia.getLabel());
    	Assert.assertEquals(asia.getComment(), actualAsia.getComment());
    	Assert.assertEquals(asia.getId(), actualAsia.getId());
    	Assert.assertNotNull(actualAsia.getParent());
    	Assert.assertEquals(actualWorld, actualAsia.getParent());
    	Assert.assertEquals(2, actualAsia.getChildren().size());

    	ArtificerOntologyClass actualJapan = actualAsia.getChildren().get(0);
    	Assert.assertEquals(japan.getUri(), actualJapan.getUri());
    	Assert.assertEquals(japan.getLabel(), actualJapan.getLabel());
    	Assert.assertEquals(japan.getComment(), actualJapan.getComment());
    	Assert.assertEquals(japan.getId(), actualJapan.getId());
    	Assert.assertNotNull(actualJapan.getParent());
    	Assert.assertEquals(actualAsia, actualJapan.getParent());
    	Assert.assertEquals(0, actualJapan.getChildren().size());

    	Set<URI> expectedJapanNormalized = new HashSet<>();
    	expectedJapanNormalized.add(actualWorld.getUri());
    	expectedJapanNormalized.add(actualAsia.getUri());
    	expectedJapanNormalized.add(actualJapan.getUri());
    	Assert.assertEquals(expectedJapanNormalized, actualJapan.normalize());
    }

    @Test
    public void testGetOntologies() throws Exception {
    	// Ensure that a "get" will return 0 ontologies first
    	List<ArtificerOntology> ontologies = persistenceManager.getOntologies();
    	Assert.assertNotNull(ontologies);
    	Assert.assertEquals(0, ontologies.size());

    	// Now add one
    	ArtificerOntology ontology = new ArtificerOntology();
    	ontology.setBase("urn:example.org/test3");
    	ontology.setLabel("Test Ontology #3");
    	ontology.setComment("This is my third test ontology.");

    	ArtificerOntologyClass world = createClass(ontology, null, "World", "World", "The entire world");
    	ArtificerOntologyClass asia = createClass(ontology, world, "Asia", "Asia", null);
    	ArtificerOntologyClass europe = createClass(ontology, world, "Europe", "Europe", "Two world wars");
    	ArtificerOntologyClass japan = createClass(ontology, asia, "Japan", "Japan", "Samurai *and* ninja?  Not fair.");
    	ArtificerOntologyClass china = createClass(ontology, asia, "China", "China", "Gunpowder!");
    	ArtificerOntologyClass uk = createClass(ontology, europe, "UnitedKingdom", "United Kingdom", "The food could be better");
    	ArtificerOntologyClass germany = createClass(ontology, europe, "Germany", "Germany", "The fatherland");

    	ontology.getRootClasses().add(world);

    	world.getChildren().add(asia);
    	world.getChildren().add(europe);
    	asia.getChildren().add(japan);
    	asia.getChildren().add(china);
    	europe.getChildren().add(uk);
    	europe.getChildren().add(germany);

    	persistenceManager.persistOntology(ontology).getUuid();

    	// Now make sure that one is returned
    	ontologies = persistenceManager.getOntologies();
    	Assert.assertNotNull(ontologies);
    	Assert.assertEquals(1, ontologies.size());
    	ArtificerOntology actual = ontologies.get(0);
    	Assert.assertEquals(ontology.getUuid(), actual.getUuid());
    	Assert.assertEquals(ontology.getBase(), actual.getBase());
    	Assert.assertEquals(ontology.getLabel(), actual.getLabel());
    	Assert.assertEquals(ontology.getComment(), actual.getComment());
    	Assert.assertEquals(ontology.getId(), actual.getId());
    	Assert.assertEquals(1, actual.getRootClasses().size());

    	// Now add another one
    	ontology = new ArtificerOntology();
    	ontology.setBase("urn:example.org/test4");
    	ontology.setLabel("Test Ontology #4");

    	ArtificerOntologyClass colors = createClass(ontology, null, "Colors", "Colors", null);
    	ArtificerOntologyClass numbers = createClass(ontology, null, "Numbers", "Numbers", null);
    	ArtificerOntologyClass red = createClass(ontology, colors, "Red", "Red", null);
    	ArtificerOntologyClass green = createClass(ontology, colors, "Green", "Green", null);
    	ArtificerOntologyClass blue = createClass(ontology, colors, "Blue", "Blue", null);
    	ArtificerOntologyClass one = createClass(ontology, numbers, "One", "One", null);
    	ArtificerOntologyClass two = createClass(ontology, numbers, "Two", "Two", null);
    	ArtificerOntologyClass three = createClass(ontology, numbers, "Three", "Three", null);

    	ontology.getRootClasses().add(colors);
    	ontology.getRootClasses().add(numbers);

    	colors.getChildren().add(red);
    	colors.getChildren().add(green);
    	colors.getChildren().add(blue);
    	numbers.getChildren().add(one);
    	numbers.getChildren().add(two);
    	numbers.getChildren().add(three);

    	persistenceManager.persistOntology(ontology).getUuid();

    	// Now we should get 2 back
    	ontologies = persistenceManager.getOntologies();
    	Assert.assertNotNull(ontologies);
    	Assert.assertEquals(2, ontologies.size());
    }

    @Test
    public void testDeleteOntologies() throws Exception {
    	// Ensure that a "get" will return 0 ontologies first
    	List<ArtificerOntology> ontologies = persistenceManager.getOntologies();
    	Assert.assertNotNull(ontologies);
    	Assert.assertEquals(0, ontologies.size());

    	// Now add one
    	ArtificerOntology ontology = new ArtificerOntology();
    	ontology.setBase("urn:example.org/test6");
    	ontology.setLabel("Test Ontology #6");
    	ontology.setComment("This is my sixth test ontology.");
    	ArtificerOntologyClass world = createClass(ontology, null, "World", "World", "The entire world");
    	ArtificerOntologyClass asia = createClass(ontology, world, "Asia", "Asia", null);
    	ArtificerOntologyClass japan = createClass(ontology, asia, "Japan", "Japan", "Samurai *and* ninja?  Not fair.");
    	ArtificerOntologyClass china = createClass(ontology, asia, "China", "China", "Gunpowder!");
    	ontology.getRootClasses().add(world);
    	world.getChildren().add(asia);
    	asia.getChildren().add(japan);
    	asia.getChildren().add(china);
    	persistenceManager.persistOntology(ontology).getUuid();

    	// Now make sure that one is returned
    	ontologies = persistenceManager.getOntologies();
    	Assert.assertNotNull(ontologies);
    	Assert.assertEquals(1, ontologies.size());

    	// Now add another one
    	ontology = new ArtificerOntology();
    	ontology.setBase("urn:example.org/test4");
    	ontology.setLabel("Test Ontology #4");
    	ArtificerOntologyClass colors = createClass(ontology, null, "Colors", "Colors", null);
    	ArtificerOntologyClass red = createClass(ontology, colors, "Red", "Red", null);
    	ArtificerOntologyClass green = createClass(ontology, colors, "Green", "Green", null);
    	ArtificerOntologyClass blue = createClass(ontology, colors, "Blue", "Blue", null);
    	ontology.getRootClasses().add(colors);
    	colors.getChildren().add(red);
    	colors.getChildren().add(green);
    	colors.getChildren().add(blue);
    	persistenceManager.persistOntology(ontology).getUuid();

    	// Now we should get 2 back
    	ontologies = persistenceManager.getOntologies();
    	Assert.assertNotNull(ontologies);
    	Assert.assertEquals(2, ontologies.size());

    	// Now delete one
    	persistenceManager.deleteOntology(ontology.getUuid());

    	// Now we should get 1 back
    	ontologies = persistenceManager.getOntologies();
    	Assert.assertNotNull(ontologies);
    	Assert.assertEquals(1, ontologies.size());
    }


    @Test
    public void testUpdate() throws Exception {
        ArtificerOntology ontology = new ArtificerOntology();
        ontology.setBase("urn:example.org/test2");
        ontology.setLabel("Test Ontology #2");
        ontology.setComment("This is my second test ontology.");

        ArtificerOntologyClass world = createClass(ontology, null, "World", "World", "The entire world");
        ArtificerOntologyClass asia = createClass(ontology, world, "Asia", "Asia", null);
        ArtificerOntologyClass europe = createClass(ontology, world, "Europe", "Europe", "Two world wars");
        ArtificerOntologyClass japan = createClass(ontology, asia, "Japan", "Japan", "Samurai *and* ninja?  Not fair.");
        ArtificerOntologyClass china = createClass(ontology, asia, "China", "China", "Gunpowder!");
        ArtificerOntologyClass uk = createClass(ontology, europe, "UnitedKingdom", "United Kingdom", "The food could be better");
        ArtificerOntologyClass germany = createClass(ontology, europe, "Germany", "Germany", "The fatherland");

        ontology.getRootClasses().add(world);

        world.getChildren().add(asia);
        world.getChildren().add(europe);
        asia.getChildren().add(japan);
        asia.getChildren().add(china);
        europe.getChildren().add(uk);
        europe.getChildren().add(germany);

        String uuid = persistenceManager.persistOntology(ontology).getUuid();
        Assert.assertNotNull(uuid);

        ArtificerOntology actual = persistenceManager.getOntology(uuid);
        Assert.assertEquals(ontology.getUuid(), actual.getUuid());
        Assert.assertEquals(ontology.getBase(), actual.getBase());
        Assert.assertEquals(ontology.getLabel(), actual.getLabel());
        Assert.assertEquals(ontology.getComment(), actual.getComment());
        Assert.assertEquals(ontology.getId(), actual.getId());
        Assert.assertEquals(1, actual.getRootClasses().size());

        ArtificerOntologyClass actualWorld = actual.getRootClasses().get(0);
        Assert.assertEquals(world.getUri(), actualWorld.getUri());
        Assert.assertEquals(world.getLabel(), actualWorld.getLabel());
        Assert.assertEquals(world.getComment(), actualWorld.getComment());
        Assert.assertEquals(world.getId(), actualWorld.getId());
        Assert.assertNull(actualWorld.getParent());
        Assert.assertEquals(2, actualWorld.getChildren().size());

        ArtificerOntologyClass actualAsia = actualWorld.getChildren().get(0);
        Assert.assertEquals(asia.getUri(), actualAsia.getUri());
        Assert.assertEquals(asia.getLabel(), actualAsia.getLabel());
        Assert.assertEquals(asia.getComment(), actualAsia.getComment());
        Assert.assertEquals(asia.getId(), actualAsia.getId());
        Assert.assertNotNull(actualAsia.getParent());
        Assert.assertEquals(actualWorld, actualAsia.getParent());
        Assert.assertEquals(2, actualAsia.getChildren().size());

        ArtificerOntologyClass actualJapan = actualAsia.getChildren().get(0);
        Assert.assertEquals(japan.getUri(), actualJapan.getUri());
        Assert.assertEquals(japan.getLabel(), actualJapan.getLabel());
        Assert.assertEquals(japan.getComment(), actualJapan.getComment());
        Assert.assertEquals(japan.getId(), actualJapan.getId());
        Assert.assertNotNull(actualJapan.getParent());
        Assert.assertEquals(actualAsia, actualJapan.getParent());
        Assert.assertEquals(0, actualJapan.getChildren().size());

        ArtificerOntologyClass northAmerica = createClass(ontology, world, "NorthAmerica", "North America", null);
        ArtificerOntologyClass sweden = createClass(ontology, europe, "Sweden", "Sweden", "Bork bork bork");
        ArtificerOntologyClass usa = createClass(ontology, northAmerica, "USA", "USA", "Cheeseburger, cheeseburger, cheeseburger...no Pepsi, Coke");
        ArtificerOntologyClass mexico = createClass(ontology, northAmerica, "Mexico", "Mexico", null);

        world.getChildren().remove(asia);
        world.getChildren().add(northAmerica);
        northAmerica.getChildren().add(usa);
        northAmerica.getChildren().add(mexico);
        europe.getChildren().remove(germany);
        europe.getChildren().add(sweden);

        persistenceManager.updateOntology(ontology);

        actual = persistenceManager.getOntology(uuid);
        Map<String, ArtificerOntologyClass> all = index(actual.getRootClasses().get(0));
        Assert.assertEquals(6, all.size());
        Assert.assertTrue(all.containsKey("NorthAmerica"));
        Assert.assertTrue(all.containsKey("USA"));
        Assert.assertTrue(all.containsKey("Mexico"));
        Assert.assertTrue(all.containsKey("Europe"));
        Assert.assertTrue(all.containsKey("UnitedKingdom"));
        Assert.assertTrue(all.containsKey("Sweden"));
        Assert.assertFalse(all.containsKey("Germany"));
        Assert.assertFalse(all.containsKey("Asia"));
        Assert.assertFalse(all.containsKey("China"));
        Assert.assertFalse(all.containsKey("Japan"));
    }

    /**
	 * Creates a test class.
	 * @param ontology
	 * @param parent
	 * @param id
	 * @param label
	 * @param comment
	 */
	private ArtificerOntologyClass createClass(ArtificerOntology ontology, ArtificerOntologyClass parent, String id, String label, String comment) {
		ArtificerOntologyClass rval = ontology.createClass(id);
		rval.setParent(parent);
		rval.setComment(comment);
		rval.setLabel(label);
		return rval;
	}

    /**
     * @param actualWorld
     */
    private Map<String, ArtificerOntologyClass> index(ArtificerOntologyClass actualWorld) {
        Map<String, ArtificerOntologyClass> all = new HashMap<String, ArtificerOntologyClass>();
        List<ArtificerOntologyClass> children = actualWorld.getChildren();
        for (ArtificerOntologyClass class1 : children) {
            all.put(class1.getId(), class1);
            all.putAll(index(class1));
        }
        return all;
    }

}
