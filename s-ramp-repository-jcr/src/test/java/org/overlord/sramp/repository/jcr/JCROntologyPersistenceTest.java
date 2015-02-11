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
package org.overlord.sramp.repository.jcr;

import org.junit.Assert;
import org.junit.Test;
import org.overlord.sramp.common.ontology.SrampOntology;
import org.overlord.sramp.common.ontology.SrampOntology.SrampOntologyClass;

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
public class JCROntologyPersistenceTest extends AbstractNoAuditingJCRPersistenceTest {

    @Test
    public void testPersistOntology_Empty() throws Exception {
    	SrampOntology ontology = new SrampOntology();
    	ontology.setBase("urn:example.org/test1");
    	ontology.setLabel("Test Ontology #1");
    	ontology.setComment("This is my first test ontology.");
    	String uuid = persistenceManager.persistOntology(ontology).getUuid();
    	Assert.assertNotNull(uuid);

    	SrampOntology actual = persistenceManager.getOntology(uuid);
    	Assert.assertEquals(ontology.getUuid(), actual.getUuid());
    	Assert.assertEquals(ontology.getBase(), actual.getBase());
    	Assert.assertEquals(ontology.getLabel(), actual.getLabel());
    	Assert.assertEquals(ontology.getComment(), actual.getComment());
    	Assert.assertEquals(ontology.getId(), actual.getId());
    }

    @Test
    public void testPersistOntology_Full() throws Exception {
    	SrampOntology ontology = new SrampOntology();
    	ontology.setBase("urn:example.org/test2");
    	ontology.setLabel("Test Ontology #2");
    	ontology.setComment("This is my second test ontology.");

    	SrampOntologyClass world = createClass(ontology, null, "World", "World", "The entire world");
    	SrampOntologyClass asia = createClass(ontology, world, "Asia", "Asia", null);
    	SrampOntologyClass europe = createClass(ontology, world, "Europe", "Europe", "Two world wars");
    	SrampOntologyClass japan = createClass(ontology, asia, "Japan", "Japan", "Samurai *and* ninja?  Not fair.");
    	SrampOntologyClass china = createClass(ontology, asia, "China", "China", "Gunpowder!");
    	SrampOntologyClass uk = createClass(ontology, europe, "UnitedKingdom", "United Kingdom", "The food could be better");
    	SrampOntologyClass germany = createClass(ontology, europe, "Germany", "Germany", "The fatherland");

    	ontology.getRootClasses().add(world);

    	world.getChildren().add(asia);
    	world.getChildren().add(europe);
    	asia.getChildren().add(japan);
    	asia.getChildren().add(china);
    	europe.getChildren().add(uk);
    	europe.getChildren().add(germany);

    	String uuid = persistenceManager.persistOntology(ontology).getUuid();
    	Assert.assertNotNull(uuid);

    	SrampOntology actual = persistenceManager.getOntology(uuid);
    	Assert.assertEquals(ontology.getUuid(), actual.getUuid());
    	Assert.assertEquals(ontology.getBase(), actual.getBase());
    	Assert.assertEquals(ontology.getLabel(), actual.getLabel());
    	Assert.assertEquals(ontology.getComment(), actual.getComment());
    	Assert.assertEquals(ontology.getId(), actual.getId());
    	Assert.assertEquals(1, actual.getRootClasses().size());

    	SrampOntologyClass actualWorld = actual.getRootClasses().get(0);
    	Assert.assertEquals(world.getUri(), actualWorld.getUri());
    	Assert.assertEquals(world.getLabel(), actualWorld.getLabel());
    	Assert.assertEquals(world.getComment(), actualWorld.getComment());
    	Assert.assertEquals(world.getId(), actualWorld.getId());
    	Assert.assertNull(actualWorld.getParent());
    	Assert.assertEquals(2, actualWorld.getChildren().size());

    	SrampOntologyClass actualAsia = actualWorld.getChildren().get(0);
    	Assert.assertEquals(asia.getUri(), actualAsia.getUri());
    	Assert.assertEquals(asia.getLabel(), actualAsia.getLabel());
    	Assert.assertEquals(asia.getComment(), actualAsia.getComment());
    	Assert.assertEquals(asia.getId(), actualAsia.getId());
    	Assert.assertNotNull(actualAsia.getParent());
    	Assert.assertEquals(actualWorld, actualAsia.getParent());
    	Assert.assertEquals(2, actualAsia.getChildren().size());

    	SrampOntologyClass actualJapan = actualAsia.getChildren().get(0);
    	Assert.assertEquals(japan.getUri(), actualJapan.getUri());
    	Assert.assertEquals(japan.getLabel(), actualJapan.getLabel());
    	Assert.assertEquals(japan.getComment(), actualJapan.getComment());
    	Assert.assertEquals(japan.getId(), actualJapan.getId());
    	Assert.assertNotNull(actualJapan.getParent());
    	Assert.assertEquals(actualAsia, actualJapan.getParent());
    	Assert.assertEquals(0, actualJapan.getChildren().size());

    	Set<URI> expectedJapanNormalized = new HashSet<URI>();
    	expectedJapanNormalized.add(actualWorld.getUri());
    	expectedJapanNormalized.add(actualAsia.getUri());
    	expectedJapanNormalized.add(actualJapan.getUri());
    	Assert.assertEquals(expectedJapanNormalized, actualJapan.normalize());
    }

    @Test
    public void testGetOntologies() throws Exception {
    	// Ensure that a "get" will return 0 ontologies first
    	List<SrampOntology> ontologies = persistenceManager.getOntologies();
    	Assert.assertNotNull(ontologies);
    	Assert.assertEquals(0, ontologies.size());

    	// Now add one
    	SrampOntology ontology = new SrampOntology();
    	ontology.setBase("urn:example.org/test3");
    	ontology.setLabel("Test Ontology #3");
    	ontology.setComment("This is my third test ontology.");

    	SrampOntologyClass world = createClass(ontology, null, "World", "World", "The entire world");
    	SrampOntologyClass asia = createClass(ontology, world, "Asia", "Asia", null);
    	SrampOntologyClass europe = createClass(ontology, world, "Europe", "Europe", "Two world wars");
    	SrampOntologyClass japan = createClass(ontology, asia, "Japan", "Japan", "Samurai *and* ninja?  Not fair.");
    	SrampOntologyClass china = createClass(ontology, asia, "China", "China", "Gunpowder!");
    	SrampOntologyClass uk = createClass(ontology, europe, "UnitedKingdom", "United Kingdom", "The food could be better");
    	SrampOntologyClass germany = createClass(ontology, europe, "Germany", "Germany", "The fatherland");

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
    	SrampOntology actual = ontologies.get(0);
    	Assert.assertEquals(ontology.getUuid(), actual.getUuid());
    	Assert.assertEquals(ontology.getBase(), actual.getBase());
    	Assert.assertEquals(ontology.getLabel(), actual.getLabel());
    	Assert.assertEquals(ontology.getComment(), actual.getComment());
    	Assert.assertEquals(ontology.getId(), actual.getId());
    	Assert.assertEquals(1, actual.getRootClasses().size());

    	// Now add another one
    	ontology = new SrampOntology();
    	ontology.setBase("urn:example.org/test4");
    	ontology.setLabel("Test Ontology #4");

    	SrampOntologyClass colors = createClass(ontology, null, "Colors", "Colors", null);
    	SrampOntologyClass numbers = createClass(ontology, null, "Numbers", "Numbers", null);
    	SrampOntologyClass red = createClass(ontology, colors, "Red", "Red", null);
    	SrampOntologyClass green = createClass(ontology, colors, "Green", "Green", null);
    	SrampOntologyClass blue = createClass(ontology, colors, "Blue", "Blue", null);
    	SrampOntologyClass one = createClass(ontology, numbers, "One", "One", null);
    	SrampOntologyClass two = createClass(ontology, numbers, "Two", "Two", null);
    	SrampOntologyClass three = createClass(ontology, numbers, "Three", "Three", null);

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
    	List<SrampOntology> ontologies = persistenceManager.getOntologies();
    	Assert.assertNotNull(ontologies);
    	Assert.assertEquals(0, ontologies.size());

    	// Now add one
    	SrampOntology ontology = new SrampOntology();
    	ontology.setBase("urn:example.org/test6");
    	ontology.setLabel("Test Ontology #6");
    	ontology.setComment("This is my sixth test ontology.");
    	SrampOntologyClass world = createClass(ontology, null, "World", "World", "The entire world");
    	SrampOntologyClass asia = createClass(ontology, world, "Asia", "Asia", null);
    	SrampOntologyClass japan = createClass(ontology, asia, "Japan", "Japan", "Samurai *and* ninja?  Not fair.");
    	SrampOntologyClass china = createClass(ontology, asia, "China", "China", "Gunpowder!");
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
    	ontology = new SrampOntology();
    	ontology.setBase("urn:example.org/test4");
    	ontology.setLabel("Test Ontology #4");
    	SrampOntologyClass colors = createClass(ontology, null, "Colors", "Colors", null);
    	SrampOntologyClass red = createClass(ontology, colors, "Red", "Red", null);
    	SrampOntologyClass green = createClass(ontology, colors, "Green", "Green", null);
    	SrampOntologyClass blue = createClass(ontology, colors, "Blue", "Blue", null);
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
        SrampOntology ontology = new SrampOntology();
        ontology.setBase("urn:example.org/test2");
        ontology.setLabel("Test Ontology #2");
        ontology.setComment("This is my second test ontology.");

        SrampOntologyClass world = createClass(ontology, null, "World", "World", "The entire world");
        SrampOntologyClass asia = createClass(ontology, world, "Asia", "Asia", null);
        SrampOntologyClass europe = createClass(ontology, world, "Europe", "Europe", "Two world wars");
        SrampOntologyClass japan = createClass(ontology, asia, "Japan", "Japan", "Samurai *and* ninja?  Not fair.");
        SrampOntologyClass china = createClass(ontology, asia, "China", "China", "Gunpowder!");
        SrampOntologyClass uk = createClass(ontology, europe, "UnitedKingdom", "United Kingdom", "The food could be better");
        SrampOntologyClass germany = createClass(ontology, europe, "Germany", "Germany", "The fatherland");

        ontology.getRootClasses().add(world);

        world.getChildren().add(asia);
        world.getChildren().add(europe);
        asia.getChildren().add(japan);
        asia.getChildren().add(china);
        europe.getChildren().add(uk);
        europe.getChildren().add(germany);

        String uuid = persistenceManager.persistOntology(ontology).getUuid();
        Assert.assertNotNull(uuid);

        SrampOntology actual = persistenceManager.getOntology(uuid);
        Assert.assertEquals(ontology.getUuid(), actual.getUuid());
        Assert.assertEquals(ontology.getBase(), actual.getBase());
        Assert.assertEquals(ontology.getLabel(), actual.getLabel());
        Assert.assertEquals(ontology.getComment(), actual.getComment());
        Assert.assertEquals(ontology.getId(), actual.getId());
        Assert.assertEquals(1, actual.getRootClasses().size());

        SrampOntologyClass actualWorld = actual.getRootClasses().get(0);
        Assert.assertEquals(world.getUri(), actualWorld.getUri());
        Assert.assertEquals(world.getLabel(), actualWorld.getLabel());
        Assert.assertEquals(world.getComment(), actualWorld.getComment());
        Assert.assertEquals(world.getId(), actualWorld.getId());
        Assert.assertNull(actualWorld.getParent());
        Assert.assertEquals(2, actualWorld.getChildren().size());

        SrampOntologyClass actualAsia = actualWorld.getChildren().get(0);
        Assert.assertEquals(asia.getUri(), actualAsia.getUri());
        Assert.assertEquals(asia.getLabel(), actualAsia.getLabel());
        Assert.assertEquals(asia.getComment(), actualAsia.getComment());
        Assert.assertEquals(asia.getId(), actualAsia.getId());
        Assert.assertNotNull(actualAsia.getParent());
        Assert.assertEquals(actualWorld, actualAsia.getParent());
        Assert.assertEquals(2, actualAsia.getChildren().size());

        SrampOntologyClass actualJapan = actualAsia.getChildren().get(0);
        Assert.assertEquals(japan.getUri(), actualJapan.getUri());
        Assert.assertEquals(japan.getLabel(), actualJapan.getLabel());
        Assert.assertEquals(japan.getComment(), actualJapan.getComment());
        Assert.assertEquals(japan.getId(), actualJapan.getId());
        Assert.assertNotNull(actualJapan.getParent());
        Assert.assertEquals(actualAsia, actualJapan.getParent());
        Assert.assertEquals(0, actualJapan.getChildren().size());

        SrampOntologyClass northAmerica = createClass(ontology, world, "NorthAmerica", "North America", null);
        SrampOntologyClass sweden = createClass(ontology, europe, "Sweden", "Sweden", "Bork bork bork");
        SrampOntologyClass usa = createClass(ontology, northAmerica, "USA", "USA", "Cheeseburger, cheeseburger, cheeseburger...no Pepsi, Coke");
        SrampOntologyClass mexico = createClass(ontology, northAmerica, "Mexico", "Mexico", null);

        world.getChildren().remove(asia);
        world.getChildren().add(northAmerica);
        northAmerica.getChildren().add(usa);
        northAmerica.getChildren().add(mexico);
        europe.getChildren().remove(germany);
        europe.getChildren().add(sweden);

        persistenceManager.updateOntology(ontology);

        actual = persistenceManager.getOntology(uuid);
        Map<String, SrampOntologyClass> all = index(actual.getRootClasses().get(0));
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
	private SrampOntologyClass createClass(SrampOntology ontology, SrampOntologyClass parent, String id, String label, String comment) {
		SrampOntologyClass rval = ontology.createClass(id);
		rval.setParent(parent);
		rval.setComment(comment);
		rval.setLabel(label);
		return rval;
	}

    /**
     * @param actualWorld
     */
    private Map<String, SrampOntologyClass> index(SrampOntologyClass actualWorld) {
        Map<String, SrampOntologyClass> all = new HashMap<String, SrampOntologyClass>();
        List<SrampOntologyClass> children = actualWorld.getChildren();
        for (SrampOntologyClass class1 : children) {
            all.put(class1.getId(), class1);
            all.putAll(index(class1));
        }
        return all;
    }

}
