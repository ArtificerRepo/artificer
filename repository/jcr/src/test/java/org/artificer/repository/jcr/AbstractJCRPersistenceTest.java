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

import org.apache.commons.io.IOUtils;
import org.artificer.repository.PersistenceManager;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Target;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XmlDocument;
import org.artificer.common.ArtifactContent;
import org.artificer.common.ArtifactTypeEnum;
import org.artificer.common.ArtificerConstants;
import org.artificer.repository.AuditManager;
import org.artificer.repository.AuditManagerFactory;
import org.artificer.repository.PersistenceFactory;
import org.artificer.repository.QueryManager;
import org.artificer.repository.QueryManagerFactory;
import org.artificer.repository.query.ArtifactSet;
import org.artificer.repository.query.ArtificerQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


/**
 * Base class for all JCR persistence tests.
 * @author eric.wittmann@redhat.com
 */
public abstract class AbstractJCRPersistenceTest {

    protected Logger log = LoggerFactory.getLogger(this.getClass());
    protected static PersistenceManager persistenceManager = null;
    protected static QueryManager queryManager = null;
    protected static AuditManager auditManager = null;

    public static void setupPersistence() {
		// use the in-memory config for unit tests
		System.setProperty("artificer.modeshape.config.url", "classpath://" + AbstractJCRPersistenceTest.class.getName()
				+ "/META-INF/modeshape-configs/junit-artificer-config.json");
        persistenceManager = PersistenceFactory.newInstance();
        queryManager = QueryManagerFactory.newInstance();
        auditManager = AuditManagerFactory.newInstance();
    }

    @Before
    public void prepForTest() {
        new JCRRepositoryCleaner().clean();
    }

    @AfterClass
    public static void cleanup() {
        persistenceManager.shutdown();
        System.clearProperty(ArtificerConstants.ARTIFICER_CONFIG_AUDITING);
    }
    
    /**
     * Adds an artifact to the repo.
     * @param resourcePath
     * @param filename
     * @param document
     * @param type
     * @throws org.artificer.common.ArtificerException
     */
    protected BaseArtifactType addArtifact(String resourcePath, String filename, XmlDocument document, BaseArtifactEnum type) throws Exception {
        InputStream contentStream = this.getClass().getResourceAsStream(resourcePath + filename);

        BaseArtifactType artifact = null;
        try {
            document.setArtifactType(type);
            document.setName(filename);
            document.setContentType("application/xml"); //$NON-NLS-1$
            // Persist the artifact
            artifact = persistenceManager.persistArtifact(document, new ArtifactContent(filename, contentStream));
            Assert.assertNotNull(artifact);
        } finally {
            IOUtils.closeQuietly(contentStream);
        }

        return artifact;
    }

    /**
     * Ensures that a single artifact exists of the given type and name.
     * @param type
     * @param name
     * @throws Exception
     */
    protected BaseArtifactType assertSingleArtifact(ArtifactTypeEnum type, String name) throws Exception {
        String q = String.format("/s-ramp/%1$s/%2$s[@name = ?]", type.getModel(), type.getType()); //$NON-NLS-1$
        ArtificerQuery query = queryManager.createQuery(q);
        query.setString(name);
        ArtifactSet artifactSet = null;
        try {
            artifactSet = query.executeQuery();
            Assert.assertEquals(1, artifactSet.size());
            BaseArtifactType arty = artifactSet.iterator().next();
            Assert.assertEquals(name, arty.getName());
            return arty;
        } finally {
            if (artifactSet != null)
                artifactSet.close();
        }
    }

    protected void assertBasic(BaseArtifactType artifact, BaseArtifactType expected) {
        assertNotNull(artifact);
        assertNotNull(artifact.getUuid());
        assertEquals(expected.getArtifactType(), artifact.getArtifactType());
        assertEquals(expected.getName(), artifact.getName());
    }

    /**
     * Gets a single artifact by UUID.
     * @param uuid
     * @throws Exception
     */
    protected BaseArtifactType getArtifactByUUID(String uuid) throws Exception {
        ArtificerQuery query = queryManager.createQuery("/s-ramp[@uuid = ?]"); //$NON-NLS-1$
        query.setString(uuid);
        ArtifactSet artifactSet = null;
        try {
            artifactSet = query.executeQuery();
            Assert.assertEquals(1, artifactSet.size());
            return artifactSet.iterator().next();
        } finally {
            if (artifactSet != null)
                artifactSet.close();
        }
    }

    /**
     * Gets an artifact by a {@link org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Target}.
     * @param target
     * @throws Exception
     */
    protected BaseArtifactType getArtifactByTarget(Target target) throws Exception {
        Assert.assertNotNull("Missing target/relationship.", target); //$NON-NLS-1$
        return getArtifactByUUID(target.getValue());
    }
}
