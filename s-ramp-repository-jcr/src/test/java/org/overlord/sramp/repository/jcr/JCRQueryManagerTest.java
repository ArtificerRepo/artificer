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

}
