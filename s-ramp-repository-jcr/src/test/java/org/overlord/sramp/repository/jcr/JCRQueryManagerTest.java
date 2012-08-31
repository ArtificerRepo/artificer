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

import java.io.InputStream;
import java.util.UUID;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.overlord.sramp.ArtifactType;
import org.overlord.sramp.repository.PersistenceFactory;
import org.overlord.sramp.repository.PersistenceManager;
import org.overlord.sramp.repository.QueryManager;
import org.overlord.sramp.repository.QueryManagerFactory;
import org.overlord.sramp.repository.query.ArtifactSet;
import org.overlord.sramp.repository.query.SrampQuery;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:kurt.stam@gmail.com">Kurt Stam</a>
 */
public class JCRQueryManagerTest {
    
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private static PersistenceManager persistenceManager = null;
    private static QueryManager queryManager = null;
    
    @BeforeClass
    public static void setup() {
        persistenceManager = PersistenceFactory.newInstance();
        queryManager = QueryManagerFactory.newInstance();
    }
    
    @Test
    public void testQueryManager() throws Exception {
    	// First, store an artifact.
        String artifactFileName = "PO.xsd";
        InputStream POXsd = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName);
        BaseArtifactType artifact = persistenceManager.persistArtifact(artifactFileName, ArtifactType.XsdDocument, POXsd);
        Assert.assertNotNull(artifact);
        log.info("persisted PO.xsd to JCR, returned artifact uuid=" + artifact.getUuid());
        
        // Now query for it
        SrampQuery query = queryManager.createQuery("/s-ramp/xsd/XsdDocument");
        ArtifactSet artifactSet = query.executeQuery();
        Assert.assertNotNull(artifactSet);
        // The assertions are tricky because the jcr repo is a singleton which may be hanging around
        // from previous unit tests in the suite.
        // TODO improve the unit testing of the JCR repo by resetting all of the services between each test
        //   Note: this can be achieved easily in maven by setting:  <forkMode>always</forkMode> in the surefire
        //         plugin.  However, when running the junit tests in Eclipse, that won't solve the issue.
        Assert.assertTrue(artifactSet.size() >= 1);
        BaseArtifactType found = null;
        for (BaseArtifactType foundArtifact : artifactSet) {
        	if (foundArtifact.getUuid().equals(artifact.getUuid())) {
        		found = foundArtifact;
        		break;
        	}
        }
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
        String artifactFileName = "PO.xsd";
        InputStream POXsd = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName);
        BaseArtifactType artifact1 = persistenceManager.persistArtifact(artifactFileName, ArtifactType.XsdDocument, POXsd);
        Assert.assertNotNull(artifact1);
        POXsd = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName);
        BaseArtifactType artifact2 = persistenceManager.persistArtifact(artifactFileName, ArtifactType.XsdDocument, POXsd);
        Assert.assertNotNull(artifact2);
        POXsd = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName);
        BaseArtifactType artifact3 = persistenceManager.persistArtifact(artifactFileName, ArtifactType.XsdDocument, POXsd);
        Assert.assertNotNull(artifact3);

        // Now update some properties on them.
        Property prop1 = new Property();
        prop1.setPropertyName("prop1");
        prop1.setPropertyValue(uniquePropVal1);
        Property prop2 = new Property();
        prop2.setPropertyName("prop2");
        prop2.setPropertyValue(uniquePropVal2);
        Property prop3 = new Property();
        prop3.setPropertyName("prop3");
        prop3.setPropertyValue(uniquePropVal3);
        // Prop3 is on all 3 artifacts, prop2 is on 2 artifacts, prop1 is on 1 artifact
        artifact1.getProperty().add(prop1);
        artifact1.getProperty().add(prop2);
        artifact1.getProperty().add(prop3);
        artifact2.getProperty().add(prop2);
        artifact2.getProperty().add(prop3);
        artifact3.getProperty().add(prop3);
        persistenceManager.updateArtifact(artifact1, ArtifactType.XsdDocument);
        persistenceManager.updateArtifact(artifact2, ArtifactType.XsdDocument);
        persistenceManager.updateArtifact(artifact3, ArtifactType.XsdDocument);

        // Now query by various properties
        SrampQuery query = queryManager.createQuery("/s-ramp/xsd/XsdDocument[@prop1 = ?]");
        query.setString(uniquePropVal1);
        ArtifactSet artifactSet = query.executeQuery();
        Assert.assertNotNull(artifactSet);
        Assert.assertEquals(1, artifactSet.size());
        
        query = queryManager.createQuery("/s-ramp/xsd/XsdDocument[@prop2 = ?]");
        query.setString(uniquePropVal2);
        artifactSet = query.executeQuery();
        Assert.assertNotNull(artifactSet);
        Assert.assertEquals(2, artifactSet.size());

        query = queryManager.createQuery("/s-ramp/xsd/XsdDocument[@prop3 = ?]");
        query.setString(uniquePropVal3);
        artifactSet = query.executeQuery();
        Assert.assertNotNull(artifactSet);
        Assert.assertEquals(3, artifactSet.size());

        query = queryManager.createQuery("/s-ramp/xsd/XsdDocument[@prop1 = ?]");
        query.setString("nomatches");
        artifactSet = query.executeQuery();
        Assert.assertNotNull(artifactSet);
        Assert.assertEquals(0, artifactSet.size());

        query = queryManager.createQuery("/s-ramp/xsd/XsdDocument[@prop2]");
        artifactSet = query.executeQuery();
        Assert.assertNotNull(artifactSet);
        Assert.assertEquals(2, artifactSet.size());

        query = queryManager.createQuery("/s-ramp/xsd/XsdDocument[@prop1 = ? and @prop2 = ?]");
        query.setString(uniquePropVal1);
        query.setString(uniquePropVal2);
        artifactSet = query.executeQuery();
        Assert.assertNotNull(artifactSet);
        Assert.assertEquals(1, artifactSet.size());

        query = queryManager.createQuery("/s-ramp/xsd/XsdDocument[@prop1 = ? or @prop2 = ?]");
        query.setString(uniquePropVal1);
        query.setString(uniquePropVal2);
        artifactSet = query.executeQuery();
        Assert.assertNotNull(artifactSet);
        Assert.assertEquals(2, artifactSet.size());

        query = queryManager.createQuery("/s-ramp/xsd/XsdDocument[@name = ?]");
        query.setString(artifactFileName);
        artifactSet = query.executeQuery();
        Assert.assertNotNull(artifactSet);
        Assert.assertTrue(artifactSet.size() >= 3);

    }

}
