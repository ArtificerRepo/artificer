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

import org.apache.commons.io.IOUtils;
import org.artificer.common.ArtifactContent;
import org.artificer.common.ArtifactType;
import org.artificer.common.ArtifactTypeEnum;
import org.artificer.common.ArtificerConstants;
import org.artificer.repository.AuditManager;
import org.artificer.repository.PersistenceManager;
import org.artificer.repository.QueryManager;
import org.artificer.repository.RepositoryProvider;
import org.artificer.repository.RepositoryProviderFactory;
import org.artificer.repository.hibernate.HibernateArtificerConstants;
import org.artificer.repository.hibernate.HibernateAuditManager;
import org.artificer.repository.hibernate.HibernatePersistenceManager;
import org.artificer.repository.hibernate.HibernateQueryManager;
import org.artificer.repository.query.ArtifactSet;
import org.artificer.repository.query.ArtificerQuery;
import org.artificer.repository.test.hibernate.HibernateRepositoryTestProvider;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Target;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XmlDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


/**
 * Base class for all persistence tests.
 *
 * @author eric.wittmann@redhat.com
 * @author Brett Meyer
 */
@RunWith(Parameterized.class)
public abstract class AbstractPersistenceTest {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    // Realistically, we should be able to parameterize the managers themselves, but having some CL issues.
    // The issues appear to be limited to IntelliJ only, but it makes debugging a nightmare.  My guess is that running
    // jUnit in the IDE causes issues when the managers are static instances...
    @Parameterized.Parameter(value = 0)
    public TestType testType;

    protected PersistenceManager persistenceManager;
    protected QueryManager queryManager;
    protected AuditManager auditManager;
    protected RepositoryTestProvider repositoryTestProvider;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        List<Object[]> data = new ArrayList<>();
        data.add(new Object[] { TestType.HIBERNATE_BLOB });
        data.add(new Object[] { TestType.HIBERNATE_FILESYSTEM });
        return data;
    }

    @Before
    public void before() throws Exception {
        Map<String, String> extraProperties = new HashMap<>();

        switch (testType) {
            case HIBERNATE_BLOB:
                persistenceManager = new HibernatePersistenceManager();
                queryManager = new HibernateQueryManager();
                auditManager = new HibernateAuditManager();

                repositoryTestProvider = new HibernateRepositoryTestProvider(extraProperties);

                break;
            case HIBERNATE_FILESYSTEM:
                persistenceManager = new HibernatePersistenceManager();
                queryManager = new HibernateQueryManager();
                auditManager = new HibernateAuditManager();

                extraProperties.put(HibernateArtificerConstants.ARTIFICER_HIBERNATE_FILESYSTEM_STORAGE_PATH,
                        "target/test/artificer-data");
                repositoryTestProvider = new HibernateRepositoryTestProvider(extraProperties);

                break;
        }

        RepositoryProviderFactory.overrideProvider(new RepositoryProvider() {
            @Override
            public String name() {
                return null;
            }
            @Override
            public PersistenceManager persistenceManager() {
                return persistenceManager;
            }
            @Override
            public QueryManager queryManager() {
                return queryManager;
            }
            @Override
            public AuditManager auditManager() {
                return auditManager;
            }
        });

        repositoryTestProvider.before();
    }

    @After
    public void after() throws Exception {
        persistenceManager.shutdown();
        repositoryTestProvider.after();
    }

    @AfterClass
    public static void cleanup() {
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

            return initArtifactAssociations(arty);
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

            return initArtifactAssociations(artifactSet.iterator().next());
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

    protected BaseArtifactType initArtifactAssociations(BaseArtifactType artifact) throws Exception {
        // When a query is used, Hibernate does not fetch the associations (on purpose -- performance optimization).
        // When they're needed, the test must call this method to full init everything by calling getArtifact.
        return persistenceManager.getArtifact(artifact.getUuid(), ArtifactType.valueOf(artifact));
    }

    public enum TestType {
        HIBERNATE_BLOB, HIBERNATE_FILESYSTEM
    }
}
