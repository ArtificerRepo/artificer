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

import java.io.InputStream;

import org.junit.Assert;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Document;
import org.overlord.sramp.common.SrampException;
import org.overlord.sramp.common.ontology.SrampOntology;
import org.overlord.sramp.repository.query.ArtifactSet;
import org.overlord.sramp.repository.query.SrampQuery;


/**
 * Tests that classifications can be queried.
 *
 * @author eric.wittmann@redhat.com
 */
public class JCRClassificationQueryTest extends AbstractNoAuditingJCRPersistenceTest {

	@Test
	public void testDerivedRelationshipQueries() throws Exception {
		createOntology();

		addDocument("no-classifications"); //$NON-NLS-1$
		addDocument("one-classification: china", "China"); //$NON-NLS-1$ //$NON-NLS-2$
		addDocument("one-classification: japan", "Japan"); //$NON-NLS-1$ //$NON-NLS-2$
		addDocument("one-classification: germany", "Germany"); //$NON-NLS-1$ //$NON-NLS-2$

		// Verify that both docs are available
		SrampQuery query = queryManager.createQuery("/s-ramp/core/Document"); //$NON-NLS-1$
		ArtifactSet artifactSet = query.executeQuery();
		Assert.assertNotNull(artifactSet);
		Assert.assertEquals(4, artifactSet.size());

		// Make sure there's only one with the given name
		query = queryManager.createQuery("/s-ramp/core/Document[@name = ?]"); //$NON-NLS-1$
		query.setString("no-classifications"); //$NON-NLS-1$
		artifactSet = query.executeQuery();
		Assert.assertNotNull(artifactSet);
		Assert.assertEquals(1, artifactSet.size());

		// Should get just the one classified by China
		query = queryManager.createQuery("/s-ramp/core/Document[s-ramp:exactlyClassifiedByAllOf(., 'China')]"); //$NON-NLS-1$
		artifactSet = query.executeQuery();
		Assert.assertNotNull(artifactSet);
		Assert.assertEquals(1, artifactSet.size());

		// Should get zero artifacts
		query = queryManager.createQuery("/s-ramp/core/Document[s-ramp:exactlyClassifiedByAllOf(., 'Asia')]"); //$NON-NLS-1$
		artifactSet = query.executeQuery();
		Assert.assertNotNull(artifactSet);
		Assert.assertEquals(0, artifactSet.size());

		// Should get just the one classified by Germany
		query = queryManager.createQuery("/s-ramp/core/Document[s-ramp:exactlyClassifiedByAllOf(., 'Germany')]"); //$NON-NLS-1$
		artifactSet = query.executeQuery();
		Assert.assertNotNull(artifactSet);
		Assert.assertEquals(1, artifactSet.size());

		// Should get zero artifacts
		query = queryManager.createQuery("/s-ramp/core/Document[s-ramp:exactlyClassifiedByAllOf(., 'China', 'Germany')]"); //$NON-NLS-1$
		artifactSet = query.executeQuery();
		Assert.assertNotNull(artifactSet);
		Assert.assertEquals(0, artifactSet.size());

		// Should get all classified artifacts
		query = queryManager.createQuery("/s-ramp/core/Document[s-ramp:classifiedByAllOf(., 'World')]"); //$NON-NLS-1$
		artifactSet = query.executeQuery();
		Assert.assertNotNull(artifactSet);
		Assert.assertEquals(3, artifactSet.size());

		// Should get two artifacts - japan and china
		query = queryManager.createQuery("/s-ramp/core/Document[s-ramp:classifiedByAllOf(., 'Asia')]"); //$NON-NLS-1$
		artifactSet = query.executeQuery();
		Assert.assertNotNull(artifactSet);
		Assert.assertEquals(2, artifactSet.size());

		// Should get two artifacts - japan and china
		query = queryManager.createQuery("/s-ramp/core/Document[s-ramp:classifiedByAnyOf(., 'Japan', 'China')]"); //$NON-NLS-1$
		artifactSet = query.executeQuery();
		Assert.assertNotNull(artifactSet);
		Assert.assertEquals(2, artifactSet.size());

	}

	/**
	 * @throws SrampException
	 */
	private SrampOntology createOntology() throws SrampException {
		SrampOntology ontology = new SrampOntology();
		ontology.setBase("urn:example.org/test2"); //$NON-NLS-1$
		ontology.setLabel("Test Ontology #2"); //$NON-NLS-1$
		ontology.setComment("This is my second test ontology."); //$NON-NLS-1$

		SrampOntology.SrampOntologyClass world = createClass(ontology, null, "World", "World", "The entire world"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		SrampOntology.SrampOntologyClass asia = createClass(ontology, world, "Asia", "Asia", null); //$NON-NLS-1$ //$NON-NLS-2$
		SrampOntology.SrampOntologyClass europe = createClass(ontology, world, "Europe", "Europe", "Two world wars"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		SrampOntology.SrampOntologyClass japan = createClass(ontology, asia, "Japan", "Japan", "Samurai *and* ninja?  Not fair."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		SrampOntology.SrampOntologyClass china = createClass(ontology, asia, "China", "China", "Gunpowder!"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		SrampOntology.SrampOntologyClass uk = createClass(ontology, europe, "UnitedKingdom", "United Kingdom", "The food could be better"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		SrampOntology.SrampOntologyClass germany = createClass(ontology, europe, "Germany", "Germany", "The fatherland"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

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
	private Document addDocument(String name, String ... classifications) throws SrampException {
		InputStream contentStream = null;
		try {
			String artifactFileName = "s-ramp-press-release.pdf"; //$NON-NLS-1$
			contentStream = this.getClass().getResourceAsStream("/sample-files/core/" + artifactFileName); //$NON-NLS-1$
			Document document = new Document();
			document.setName(name);
			document.setArtifactType(BaseArtifactEnum.DOCUMENT);
			for (String classification : classifications) {
				document.getClassifiedBy().add(classification);
			}
			return (Document) persistenceManager.persistArtifact(document, contentStream);
		} finally {
			IOUtils.closeQuietly(contentStream);
		}
	}

}
