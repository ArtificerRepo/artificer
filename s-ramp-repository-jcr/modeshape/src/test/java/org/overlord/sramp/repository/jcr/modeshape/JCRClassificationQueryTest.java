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

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Document;
import org.overlord.sramp.common.ArtifactContent;
import org.overlord.sramp.common.SrampException;
import org.overlord.sramp.common.ontology.SrampOntology;
import org.overlord.sramp.repository.query.ArtifactSet;
import org.overlord.sramp.repository.query.SrampQuery;

import java.io.InputStream;
import java.util.List;


/**
 * Tests that classifications can be queried.
 *
 * @author eric.wittmann@redhat.com
 */
public class JCRClassificationQueryTest extends AbstractNoAuditingJCRPersistenceTest {

	@Test
	public void testDerivedRelationshipQueries() throws Exception {
		createOntology();

        Document doc = addDocument("no-classifications");
        Document docChina = addDocument("one-classification: china", "China");
        Document docJapan = addDocument("one-classification: japan", "Japan");
        Document docGermany = addDocument("one-classification: germany", "Germany");

		// Verify that the docs are available
		SrampQuery query = queryManager.createQuery("/s-ramp/core/Document");
		ArtifactSet artifactSet = query.executeQuery();
		Assert.assertNotNull(artifactSet);
		Assert.assertEquals(4, artifactSet.size());

		// Make sure there's only one with the given name
		query = queryManager.createQuery("/s-ramp/core/Document[@name = ?]");
		query.setString("no-classifications");
		artifactSet = query.executeQuery();
        assertResults(artifactSet, doc);

		// Should get just the one classified by China
		query = queryManager.createQuery("/s-ramp/core/Document[s-ramp:exactlyClassifiedByAllOf(., 'China')]");
		artifactSet = query.executeQuery();
        assertResults(artifactSet, docChina);

		// Should get zero artifacts
        query = queryManager.createQuery("/s-ramp/core/Document[s-ramp:exactlyClassifiedByAllOf(., 'Asia')]");
		artifactSet = query.executeQuery();
		Assert.assertNotNull(artifactSet);
		Assert.assertEquals(0, artifactSet.size());

		// Should get just the one classified by Germany
		query = queryManager.createQuery("/s-ramp/core/Document[s-ramp:exactlyClassifiedByAllOf(., 'Germany')]");
		artifactSet = query.executeQuery();
        assertResults(artifactSet, docGermany);

		// Should get zero artifacts
        query = queryManager.createQuery("/s-ramp/core/Document[s-ramp:exactlyClassifiedByAllOf(., 'China', 'Germany')]");
		artifactSet = query.executeQuery();
		Assert.assertNotNull(artifactSet);
		Assert.assertEquals(0, artifactSet.size());

		// Should get all classified artifacts
		query = queryManager.createQuery("/s-ramp/core/Document[s-ramp:classifiedByAllOf(., 'World')]");
		artifactSet = query.executeQuery();
        assertResults(artifactSet, docJapan, docChina, docGermany);

        // Should get two artifacts - japan and china
		query = queryManager.createQuery("/s-ramp/core/Document[s-ramp:classifiedByAllOf(., 'Asia')]");
		artifactSet = query.executeQuery();
        assertResults(artifactSet, docJapan, docChina);

		// Should get two artifacts - japan and china
		query = queryManager.createQuery("/s-ramp/core/Document[s-ramp:classifiedByAnyOf(., 'Japan', 'China')]");
		artifactSet = query.executeQuery();
        assertResults(artifactSet, docJapan, docChina);

        // Test not()
        query = queryManager.createQuery("/s-ramp/core/Document[xp2:not(s-ramp:classifiedByAnyOf(., 'Japan', 'China'))]");
        artifactSet = query.executeQuery();
        assertResults(artifactSet, doc, docGermany);
	}

    private void assertResults(ArtifactSet artifactSet, Document... docs) throws Exception {
        Assert.assertNotNull(artifactSet);
        Assert.assertEquals(docs.length, artifactSet.size());
        List<BaseArtifactType> artifacts = artifactSet.list();
        for (BaseArtifactType artifact : artifacts) {
            boolean found = false;
            for (Document doc : docs) {
                if (doc.getUuid().equals(artifact.getUuid())) {
                    found = true;
                }
            }
            Assert.assertTrue(found);
        }
    }

	/**
	 * @throws SrampException
	 */
	private SrampOntology createOntology() throws SrampException {
		SrampOntology ontology = new SrampOntology();
		ontology.setBase("urn:example.org/test2");
		ontology.setLabel("Test Ontology #2");
		ontology.setComment("This is my second test ontology.");

		SrampOntology.SrampOntologyClass world = createClass(ontology, null, "World", "World", "The entire world");
		SrampOntology.SrampOntologyClass asia = createClass(ontology, world, "Asia", "Asia", null);
		SrampOntology.SrampOntologyClass europe = createClass(ontology, world, "Europe", "Europe", "Two world wars");
		SrampOntology.SrampOntologyClass japan = createClass(ontology, asia, "Japan", "Japan", "Samurai *and* ninja?  Not fair.");
		SrampOntology.SrampOntologyClass china = createClass(ontology, asia, "China", "China", "Gunpowder!");
		SrampOntology.SrampOntologyClass uk = createClass(ontology, europe, "UnitedKingdom", "United Kingdom", "The food could be better");
		SrampOntology.SrampOntologyClass germany = createClass(ontology, europe, "Germany", "Germany", "The fatherland");

		ontology.getRootClasses().add(world);

		world.getChildren().add(asia);
		world.getChildren().add(europe);
		asia.getChildren().add(japan);
		asia.getChildren().add(china);
		europe.getChildren().add(uk);
		europe.getChildren().add(germany);

		return persistenceManager.persistOntology(ontology);
	}

	/**
	 * Creates a test class.
	 * @param ontology
	 * @param parent
	 * @param id
	 * @param label
	 * @param comment
	 */
	private SrampOntology.SrampOntologyClass createClass(SrampOntology ontology, SrampOntology.SrampOntologyClass parent, String id, String label, String comment) {
		SrampOntology.SrampOntologyClass rval = ontology.createClass(id);
		rval.setParent(parent);
		rval.setComment(comment);
		rval.setLabel(label);
		return rval;
	}

	/**
	 * @throws SrampException
	 */
	private Document addDocument(String name, String ... classifications) throws Exception {
		InputStream contentStream = null;
		try {
			String artifactFileName = "s-ramp-press-release.pdf";
			contentStream = this.getClass().getResourceAsStream("/sample-files/core/" + artifactFileName);
			Document document = new Document();
			document.setName(name);
			document.setArtifactType(BaseArtifactEnum.DOCUMENT);
			for (String classification : classifications) {
				document.getClassifiedBy().add(classification);
			}
			return (Document) persistenceManager.persistArtifact(document, new ArtifactContent(artifactFileName, contentStream));
		} finally {
			IOUtils.closeQuietly(contentStream);
		}
	}

}
