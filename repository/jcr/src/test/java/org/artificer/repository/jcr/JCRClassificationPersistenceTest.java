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
package org.artificer.repository.jcr;

import org.junit.Assert;
import org.junit.Test;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Document;
import org.artificer.common.ArtifactContent;
import org.artificer.common.ArtifactType;
import org.artificer.common.ArtificerException;
import org.artificer.common.ontology.ArtificerOntology;

import java.io.InputStream;


/**
 * Unit test for JCR persistence of S-RAMP classifications.
 *
 * @author eric.wittmann@redhat.com
 */
public class JCRClassificationPersistenceTest extends AbstractNoAuditingJCRPersistenceTest {

    @Test
    public void testPersistClassifications() throws Exception {
    	ArtificerOntology ontology = createOntology();

        String artifactFileName = "s-ramp-press-release.pdf";
        InputStream contentStream = this.getClass().getResourceAsStream("/sample-files/core/" + artifactFileName);
        Document document = new Document();
        document.setName(artifactFileName);
        document.setArtifactType(BaseArtifactEnum.DOCUMENT);
        document.getClassifiedBy().add(ontology.findClass("China").getUri().toString());

        BaseArtifactType artifact = persistenceManager.persistArtifact(document, new ArtifactContent(artifactFileName, contentStream));
        Assert.assertNotNull(artifact);
        if (log.isDebugEnabled()) {
            persistenceManager.printArtifactGraph(artifact.getUuid(), ArtifactType.Document());
        }

        artifact = persistenceManager.getArtifact(artifact.getUuid(), ArtifactType.Document());

        Assert.assertNotNull(artifact.getClassifiedBy());
        Assert.assertEquals(1, artifact.getClassifiedBy().size());
        Assert.assertEquals("urn:example.org/test2#China", artifact.getClassifiedBy().get(0));
    }

	/**
	 * @throws org.artificer.common.ArtificerException
	 */
	private ArtificerOntology createOntology() throws ArtificerException {
    	ArtificerOntology ontology = new ArtificerOntology();
    	ontology.setBase("urn:example.org/test2");
    	ontology.setLabel("Test Ontology #2");
    	ontology.setComment("This is my second test ontology.");

    	ArtificerOntology.ArtificerOntologyClass world = createClass(ontology, null, "World", "World", "The entire world");
    	ArtificerOntology.ArtificerOntologyClass asia = createClass(ontology, world, "Asia", "Asia", null);
    	ArtificerOntology.ArtificerOntologyClass europe = createClass(ontology, world, "Europe", "Europe", "Two world wars");
    	ArtificerOntology.ArtificerOntologyClass japan = createClass(ontology, asia, "Japan", "Japan", "Samurai *and* ninja?  Not fair.");
    	ArtificerOntology.ArtificerOntologyClass china = createClass(ontology, asia, "China", "China", "Gunpowder!");
    	ArtificerOntology.ArtificerOntologyClass uk = createClass(ontology, europe, "UnitedKingdom", "United Kingdom", "The food could be better");
    	ArtificerOntology.ArtificerOntologyClass germany = createClass(ontology, europe, "Germany", "Germany", "The fatherland");

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
	private ArtificerOntology.ArtificerOntologyClass createClass(ArtificerOntology ontology, ArtificerOntology.ArtificerOntologyClass parent, String id, String label, String comment) {
		ArtificerOntology.ArtificerOntologyClass rval = ontology.createClass(id);
		rval.setParent(parent);
		rval.setComment(comment);
		rval.setLabel(label);
		return rval;
	}

}
