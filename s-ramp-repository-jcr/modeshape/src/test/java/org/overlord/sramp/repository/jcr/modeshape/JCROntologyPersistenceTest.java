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
package org.overlord.sramp.repository.jcr.modeshape;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.overlord.sramp.common.ontology.SrampOntology;
import org.overlord.sramp.common.ontology.SrampOntology.Class;


/**
 * Unit test for JCR persistence of S-RAMP ontologies.
 *
 * @author eric.wittmann@redhat.com
 */
public class JCROntologyPersistenceTest extends AbstractNoAuditingJCRPersistenceTest {

    @Test
    public void testPersistOntology_Empty() throws Exception {
    	SrampOntology ontology = new SrampOntology();
    	ontology.setBase("urn:example.org/test1"); //$NON-NLS-1$
    	ontology.setLabel("Test Ontology #1"); //$NON-NLS-1$
    	ontology.setComment("This is my first test ontology."); //$NON-NLS-1$
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
    	ontology.setBase("urn:example.org/test2"); //$NON-NLS-1$
    	ontology.setLabel("Test Ontology #2"); //$NON-NLS-1$
    	ontology.setComment("This is my second test ontology."); //$NON-NLS-1$

    	SrampOntology.Class world = createClass(ontology, null, "World", "World", "The entire world"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    	SrampOntology.Class asia = createClass(ontology, world, "Asia", "Asia", null); //$NON-NLS-1$ //$NON-NLS-2$
    	SrampOntology.Class europe = createClass(ontology, world, "Europe", "Europe", "Two world wars"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    	SrampOntology.Class japan = createClass(ontology, asia, "Japan", "Japan", "Samurai *and* ninja?  Not fair."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    	SrampOntology.Class china = createClass(ontology, asia, "China", "China", "Gunpowder!"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    	SrampOntology.Class uk = createClass(ontology, europe, "UnitedKingdom", "United Kingdom", "The food could be better"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    	SrampOntology.Class germany = createClass(ontology, europe, "Germany", "Germany", "The fatherland"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

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

    	SrampOntology.Class actualWorld = actual.getRootClasses().get(0);
    	Assert.assertEquals(world.getUri(), actualWorld.getUri());
    	Assert.assertEquals(world.getLabel(), actualWorld.getLabel());
    	Assert.assertEquals(world.getComment(), actualWorld.getComment());
    	Assert.assertEquals(world.getId(), actualWorld.getId());
    	Assert.assertNull(actualWorld.getParent());
    	Assert.assertEquals(2, actualWorld.getChildren().size());

    	SrampOntology.Class actualAsia = actualWorld.getChildren().get(0);
    	Assert.assertEquals(asia.getUri(), actualAsia.getUri());
    	Assert.assertEquals(asia.getLabel(), actualAsia.getLabel());
    	Assert.assertEquals(asia.getComment(), actualAsia.getComment());
    	Assert.assertEquals(asia.getId(), actualAsia.getId());
    	Assert.assertNotNull(actualAsia.getParent());
    	Assert.assertEquals(actualWorld, actualAsia.getParent());
    	Assert.assertEquals(2, actualAsia.getChildren().size());

    	SrampOntology.Class actualJapan = actualAsia.getChildren().get(0);
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
    	ontology.setBase("urn:example.org/test3"); //$NON-NLS-1$
    	ontology.setLabel("Test Ontology #3"); //$NON-NLS-1$
    	ontology.setComment("This is my third test ontology."); //$NON-NLS-1$

    	SrampOntology.Class world = createClass(ontology, null, "World", "World", "The entire world"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    	SrampOntology.Class asia = createClass(ontology, world, "Asia", "Asia", null); //$NON-NLS-1$ //$NON-NLS-2$
    	SrampOntology.Class europe = createClass(ontology, world, "Europe", "Europe", "Two world wars"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    	SrampOntology.Class japan = createClass(ontology, asia, "Japan", "Japan", "Samurai *and* ninja?  Not fair."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    	SrampOntology.Class china = createClass(ontology, asia, "China", "China", "Gunpowder!"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    	SrampOntology.Class uk = createClass(ontology, europe, "UnitedKingdom", "United Kingdom", "The food could be better"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    	SrampOntology.Class germany = createClass(ontology, europe, "Germany", "Germany", "The fatherland"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

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
    	ontology.setBase("urn:example.org/test4"); //$NON-NLS-1$
    	ontology.setLabel("Test Ontology #4"); //$NON-NLS-1$

    	SrampOntology.Class colors = createClass(ontology, null, "Colors", "Colors", null); //$NON-NLS-1$ //$NON-NLS-2$
    	SrampOntology.Class numbers = createClass(ontology, null, "Numbers", "Numbers", null); //$NON-NLS-1$ //$NON-NLS-2$
    	SrampOntology.Class red = createClass(ontology, colors, "Red", "Red", null); //$NON-NLS-1$ //$NON-NLS-2$
    	SrampOntology.Class green = createClass(ontology, colors, "Green", "Green", null); //$NON-NLS-1$ //$NON-NLS-2$
    	SrampOntology.Class blue = createClass(ontology, colors, "Blue", "Blue", null); //$NON-NLS-1$ //$NON-NLS-2$
    	SrampOntology.Class one = createClass(ontology, numbers, "One", "One", null); //$NON-NLS-1$ //$NON-NLS-2$
    	SrampOntology.Class two = createClass(ontology, numbers, "Two", "Two", null); //$NON-NLS-1$ //$NON-NLS-2$
    	SrampOntology.Class three = createClass(ontology, numbers, "Three", "Three", null); //$NON-NLS-1$ //$NON-NLS-2$

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
    	ontology.setBase("urn:example.org/test6"); //$NON-NLS-1$
    	ontology.setLabel("Test Ontology #6"); //$NON-NLS-1$
    	ontology.setComment("This is my sixth test ontology."); //$NON-NLS-1$
    	SrampOntology.Class world = createClass(ontology, null, "World", "World", "The entire world"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    	SrampOntology.Class asia = createClass(ontology, world, "Asia", "Asia", null); //$NON-NLS-1$ //$NON-NLS-2$
    	SrampOntology.Class japan = createClass(ontology, asia, "Japan", "Japan", "Samurai *and* ninja?  Not fair."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    	SrampOntology.Class china = createClass(ontology, asia, "China", "China", "Gunpowder!"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
    	ontology.setBase("urn:example.org/test4"); //$NON-NLS-1$
    	ontology.setLabel("Test Ontology #4"); //$NON-NLS-1$
    	SrampOntology.Class colors = createClass(ontology, null, "Colors", "Colors", null); //$NON-NLS-1$ //$NON-NLS-2$
    	SrampOntology.Class red = createClass(ontology, colors, "Red", "Red", null); //$NON-NLS-1$ //$NON-NLS-2$
    	SrampOntology.Class green = createClass(ontology, colors, "Green", "Green", null); //$NON-NLS-1$ //$NON-NLS-2$
    	SrampOntology.Class blue = createClass(ontology, colors, "Blue", "Blue", null); //$NON-NLS-1$ //$NON-NLS-2$
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
        ontology.setBase("urn:example.org/test2"); //$NON-NLS-1$
        ontology.setLabel("Test Ontology #2"); //$NON-NLS-1$
        ontology.setComment("This is my second test ontology."); //$NON-NLS-1$

        SrampOntology.Class world = createClass(ontology, null, "World", "World", "The entire world"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        SrampOntology.Class asia = createClass(ontology, world, "Asia", "Asia", null); //$NON-NLS-1$ //$NON-NLS-2$
        SrampOntology.Class europe = createClass(ontology, world, "Europe", "Europe", "Two world wars"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        SrampOntology.Class japan = createClass(ontology, asia, "Japan", "Japan", "Samurai *and* ninja?  Not fair."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        SrampOntology.Class china = createClass(ontology, asia, "China", "China", "Gunpowder!"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        SrampOntology.Class uk = createClass(ontology, europe, "UnitedKingdom", "United Kingdom", "The food could be better"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        SrampOntology.Class germany = createClass(ontology, europe, "Germany", "Germany", "The fatherland"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

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

        SrampOntology.Class actualWorld = actual.getRootClasses().get(0);
        Assert.assertEquals(world.getUri(), actualWorld.getUri());
        Assert.assertEquals(world.getLabel(), actualWorld.getLabel());
        Assert.assertEquals(world.getComment(), actualWorld.getComment());
        Assert.assertEquals(world.getId(), actualWorld.getId());
        Assert.assertNull(actualWorld.getParent());
        Assert.assertEquals(2, actualWorld.getChildren().size());

        SrampOntology.Class actualAsia = actualWorld.getChildren().get(0);
        Assert.assertEquals(asia.getUri(), actualAsia.getUri());
        Assert.assertEquals(asia.getLabel(), actualAsia.getLabel());
        Assert.assertEquals(asia.getComment(), actualAsia.getComment());
        Assert.assertEquals(asia.getId(), actualAsia.getId());
        Assert.assertNotNull(actualAsia.getParent());
        Assert.assertEquals(actualWorld, actualAsia.getParent());
        Assert.assertEquals(2, actualAsia.getChildren().size());

        SrampOntology.Class actualJapan = actualAsia.getChildren().get(0);
        Assert.assertEquals(japan.getUri(), actualJapan.getUri());
        Assert.assertEquals(japan.getLabel(), actualJapan.getLabel());
        Assert.assertEquals(japan.getComment(), actualJapan.getComment());
        Assert.assertEquals(japan.getId(), actualJapan.getId());
        Assert.assertNotNull(actualJapan.getParent());
        Assert.assertEquals(actualAsia, actualJapan.getParent());
        Assert.assertEquals(0, actualJapan.getChildren().size());

        SrampOntology.Class northAmerica = createClass(ontology, world, "NorthAmerica", "North America", null); //$NON-NLS-1$ //$NON-NLS-2$
        SrampOntology.Class sweden = createClass(ontology, europe, "Sweden", "Sweden", "Bork bork bork"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        SrampOntology.Class usa = createClass(ontology, northAmerica, "USA", "USA", "Cheeseburger, cheeseburger, cheeseburger...no Pepsi, Coke"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        SrampOntology.Class mexico = createClass(ontology, northAmerica, "Mexico", "Mexico", null); //$NON-NLS-1$ //$NON-NLS-2$

        world.getChildren().remove(asia);
        world.getChildren().add(northAmerica);
        northAmerica.getChildren().add(usa);
        northAmerica.getChildren().add(mexico);
        europe.getChildren().remove(germany);
        europe.getChildren().add(sweden);

        persistenceManager.updateOntology(ontology);

        actual = persistenceManager.getOntology(uuid);
        Map<String, SrampOntology.Class> all = index(actual.getRootClasses().get(0));
        Assert.assertEquals(6, all.size());
        Assert.assertTrue(all.containsKey("NorthAmerica")); //$NON-NLS-1$
        Assert.assertTrue(all.containsKey("USA")); //$NON-NLS-1$
        Assert.assertTrue(all.containsKey("Mexico")); //$NON-NLS-1$
        Assert.assertTrue(all.containsKey("Europe")); //$NON-NLS-1$
        Assert.assertTrue(all.containsKey("UnitedKingdom")); //$NON-NLS-1$
        Assert.assertTrue(all.containsKey("Sweden")); //$NON-NLS-1$
        Assert.assertFalse(all.containsKey("Germany")); //$NON-NLS-1$
        Assert.assertFalse(all.containsKey("Asia")); //$NON-NLS-1$
        Assert.assertFalse(all.containsKey("China")); //$NON-NLS-1$
        Assert.assertFalse(all.containsKey("Japan")); //$NON-NLS-1$
    }

    /**
	 * Creates a test class.
	 * @param ontology
	 * @param parent
	 * @param id
	 * @param label
	 * @param comment
	 */
	private SrampOntology.Class createClass(SrampOntology ontology, SrampOntology.Class parent, String id, String label, String comment) {
		SrampOntology.Class rval = ontology.createClass(id);
		rval.setParent(parent);
		rval.setComment(comment);
		rval.setLabel(label);
		return rval;
	}

    /**
     * @param actualWorld
     */
    private Map<String, Class> index(Class actualWorld) {
        Map<String, Class> all = new HashMap<String, Class>();
        List<Class> children = actualWorld.getChildren();
        for (Class class1 : children) {
            all.put(class1.getId(), class1);
            all.putAll(index(class1));
        }
        return all;
    }

}
