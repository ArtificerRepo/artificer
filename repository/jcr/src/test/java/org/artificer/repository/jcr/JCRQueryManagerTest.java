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

import org.artificer.common.ArtifactContent;
import org.artificer.common.ArtifactType;
import org.artificer.common.ArtificerModelUtils;
import org.artificer.repository.query.ArtifactSet;
import org.artificer.repository.query.ArtificerQuery;
import org.junit.Assert;
import org.junit.Test;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XmlDocument;

import java.io.InputStream;
import java.util.Date;
import java.util.UUID;


/**
 * @author <a href="mailto:kurt.stam@gmail.com">Kurt Stam</a>
 */
public class JCRQueryManagerTest extends AbstractNoAuditingJCRPersistenceTest {

    /**
     * Tests the query manager.
     * @throws Exception
     */
    @Test
    public void testQueryManager() throws Exception {
    	// First, store an artifact.
		BaseArtifactType artifact = createXmlDocument("PO.xml", 1);
        log.info("persisted to JCR, returned artifact uuid=" + artifact.getUuid());

        // Now query for it
        ArtificerQuery query = queryManager.createQuery("/s-ramp/core/XmlDocument");
        ArtifactSet artifactSet = query.executeQuery();
        Assert.assertNotNull(artifactSet);

        Assert.assertEquals(1, artifactSet.size());
        BaseArtifactType found = artifactSet.iterator().next();
        Assert.assertNotNull("Expected artifact not found in artifact set.", found);
        Assert.assertEquals(artifact.getUuid(), found.getUuid());
        Assert.assertEquals(artifact.getName(), found.getName());
        Assert.assertEquals(artifact.getDescription(), found.getDescription());
        Assert.assertEquals(artifact.getLastModifiedBy(), found.getLastModifiedBy());
    }

    /**
     * Tests querying by s-ramp properties.
     * @throws Exception
     */
    @Test
    public void testQueryByProperty() throws Exception {
    	String uniquePropVal1 = UUID.randomUUID().toString();
    	String uniquePropVal2 = UUID.randomUUID().toString();
    	String uniquePropVal3 = UUID.randomUUID().toString();

    	// First, store 3 artifacts
		BaseArtifactType artifact1 = createXmlDocument("PO.xml", 1);
		BaseArtifactType artifact2 = createXmlDocument("PO.xml", 2);
		BaseArtifactType artifact3 = createXmlDocument("PO.xml", 3);

        // Now update some properties on them.
        // Prop3 is on all 3 artifacts, prop2 is on 2 artifacts, prop1 is on 1 artifact
        ArtificerModelUtils.setCustomProperty(artifact1, "prop1", uniquePropVal1);
        ArtificerModelUtils.setCustomProperty(artifact1, "prop2", uniquePropVal2);
        ArtificerModelUtils.setCustomProperty(artifact1, "prop3", uniquePropVal3);
        ArtificerModelUtils.setCustomProperty(artifact2, "prop2", uniquePropVal2);
        ArtificerModelUtils.setCustomProperty(artifact2, "prop3", uniquePropVal3);
        ArtificerModelUtils.setCustomProperty(artifact3, "prop3", uniquePropVal3);

        persistenceManager.updateArtifact(artifact1, ArtifactType.XmlDocument());
        persistenceManager.updateArtifact(artifact2, ArtifactType.XmlDocument());
        persistenceManager.updateArtifact(artifact3, ArtifactType.XmlDocument());

        // Now query by various properties
        persistenceManager.printArtifactGraph(artifact1.getUuid(), ArtifactType.XmlDocument());
        ArtificerQuery query = queryManager.createQuery("/s-ramp/core/XmlDocument[@prop1 = ?]");
        query.setString(uniquePropVal1);
        ArtifactSet artifactSet = query.executeQuery();
        Assert.assertNotNull(artifactSet);
        Assert.assertEquals(1, artifactSet.size());

        query = queryManager.createQuery("/s-ramp/core/XmlDocument[@prop2 = ?]");
        query.setString(uniquePropVal2);
        artifactSet = query.executeQuery();
        Assert.assertNotNull(artifactSet);
        Assert.assertEquals(2, artifactSet.size());

        query = queryManager.createQuery("/s-ramp/core/XmlDocument[@prop3 = ?]");
        query.setString(uniquePropVal3);
        artifactSet = query.executeQuery();
        Assert.assertNotNull(artifactSet);
        Assert.assertEquals(3, artifactSet.size());

        query = queryManager.createQuery("/s-ramp/core/XmlDocument[@prop1 = ?]");
        query.setString("nomatches");
        artifactSet = query.executeQuery();
        Assert.assertNotNull(artifactSet);
        Assert.assertEquals(0, artifactSet.size());

        query = queryManager.createQuery("/s-ramp/core/XmlDocument[@prop2]");
        artifactSet = query.executeQuery();
        Assert.assertNotNull(artifactSet);
        Assert.assertEquals(2, artifactSet.size());

        query = queryManager.createQuery("/s-ramp/core/XmlDocument[@prop1 = ? and @prop2 = ?]");
        query.setString(uniquePropVal1);
        query.setString(uniquePropVal2);
        artifactSet = query.executeQuery();
        Assert.assertNotNull(artifactSet);
        Assert.assertEquals(1, artifactSet.size());

        query = queryManager.createQuery("/s-ramp/core/XmlDocument[@prop1 = ? or @prop2 = ?]");
        query.setString(uniquePropVal1);
        query.setString(uniquePropVal2);
        artifactSet = query.executeQuery();
        Assert.assertNotNull(artifactSet);
        Assert.assertEquals(2, artifactSet.size());

        query = queryManager.createQuery("/s-ramp/core/XmlDocument[@version = ?]");
        query.setString("1.0.3");
        artifactSet = query.executeQuery();
        Assert.assertNotNull(artifactSet);
        Assert.assertEquals(3, artifactSet.size());

        query = queryManager.createQuery("/s-ramp/core/XmlDocument[@lastModifiedTimestamp < ?]");
        query.setDate(new Date(System.currentTimeMillis() + 86400000L));
        artifactSet = query.executeQuery();
        Assert.assertNotNull(artifactSet);
        Assert.assertTrue(artifactSet.size() >= 3);

        query = queryManager.createQuery("/s-ramp/core/XmlDocument[@lastModifiedTimestamp > ?]");
        query.setDate(new Date(System.currentTimeMillis() + 86400000L));
        artifactSet = query.executeQuery();
        Assert.assertNotNull(artifactSet);
        Assert.assertEquals(0, artifactSet.size());

        // Negation by property existence
        query = queryManager.createQuery("/s-ramp/core/XmlDocument[xp2:not(@prop1)]");
        artifactSet = query.executeQuery();
        Assert.assertNotNull(artifactSet);
        Assert.assertEquals(2, artifactSet.size());

        // Negation by property value
        query = queryManager.createQuery("/s-ramp/core/XmlDocument[xp2:not(@prop1 = ?)]");
        query.setString("nomatches");
        artifactSet = query.executeQuery();
        Assert.assertNotNull(artifactSet);
        Assert.assertEquals(3, artifactSet.size());

        // AND'd negation by property value
        query = queryManager.createQuery("/s-ramp/core/XmlDocument[xp2:not(@prop1 = ? and @prop2 = ?)]");
        query.setString(uniquePropVal1);
        query.setString(uniquePropVal2);
        artifactSet = query.executeQuery();
        Assert.assertNotNull(artifactSet);
        Assert.assertEquals(2, artifactSet.size());

    }

    @Test
    public void testFreeTextSearch() throws Exception {
        // First, store 3 artifacts
        BaseArtifactType artifact1 = createXmlDocument("PO.xml", 1); // content contains "Lawn Mower" and "Baby Monitor"
        BaseArtifactType artifact2 = createXmlDocument("PO.xml", 2); // content contains "Lawn Mower" and "Baby Monitor"
        BaseArtifactType artifact3 = createXmlDocument("PO2.xml", 3); // content contains "Leaf Blower" and "Baby Monitor"

        // Now update some properties on them
        ArtificerModelUtils.setCustomProperty(artifact1, "prop1", "fizz test");
        ArtificerModelUtils.setCustomProperty(artifact2, "prop1", "buzz test");
        ArtificerModelUtils.setCustomProperty(artifact3, "prop1", "fizz buzz test");

        persistenceManager.updateArtifact(artifact1, ArtifactType.XmlDocument());
        persistenceManager.updateArtifact(artifact2, ArtifactType.XmlDocument());
        persistenceManager.updateArtifact(artifact3, ArtifactType.XmlDocument());

        // full-text, using metadata
        ArtificerQuery query = queryManager.createQuery("/s-ramp[xp2:matches(., ?)]");
        query.setString("fizz");
        ArtifactSet artifactSet = query.executeQuery();
        Assert.assertNotNull(artifactSet);
        Assert.assertEquals(2, artifactSet.size());
        query = queryManager.createQuery("/s-ramp[xp2:matches(., ?)]");
        query.setString("buzz");
        artifactSet = query.executeQuery();
        Assert.assertNotNull(artifactSet);
        Assert.assertEquals(2, artifactSet.size());
        query = queryManager.createQuery("/s-ramp[xp2:matches(., ?)]");
        query.setString("test");
        artifactSet = query.executeQuery();
        Assert.assertNotNull(artifactSet);
        Assert.assertEquals(3, artifactSet.size());

        // full-text, using content
        query = queryManager.createQuery("/s-ramp[xp2:matches(., ?)]");
        query.setString("Lawn Mower");
        artifactSet = query.executeQuery();
        Assert.assertNotNull(artifactSet);
        Assert.assertEquals(2, artifactSet.size());
        query = queryManager.createQuery("/s-ramp[xp2:matches(., ?)]");
        query.setString("Leaf Blower");
        artifactSet = query.executeQuery();
        Assert.assertNotNull(artifactSet);
        Assert.assertEquals(1, artifactSet.size());
        query = queryManager.createQuery("/s-ramp[xp2:matches(., ?)]");
        query.setString("Baby Monitor");
        artifactSet = query.executeQuery();
        Assert.assertNotNull(artifactSet);
        Assert.assertEquals(3, artifactSet.size());
    }

	/**
	 * @throws org.artificer.common.ArtificerException
	 */
	private BaseArtifactType createXmlDocument(String artifactFileName, int idx) throws Exception {
		InputStream content = this.getClass().getResourceAsStream("/sample-files/core/" + artifactFileName);
        XmlDocument document = (XmlDocument) ArtifactType.XmlDocument().newArtifactInstance();
        document.setName(artifactFileName + "-" + idx);
        document.setVersion("1.0.3");
        document.setArtifactType(BaseArtifactEnum.XML_DOCUMENT);
        BaseArtifactType artifact1 = persistenceManager.persistArtifact(document, new ArtifactContent(artifactFileName, content));
        Assert.assertNotNull(artifact1);
		return artifact1;
	}

}
