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

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.overlord.sramp.repository.AuditManager;
import org.overlord.sramp.repository.AuditManagerFactory;
import org.overlord.sramp.repository.DerivedArtifacts;
import org.overlord.sramp.repository.DerivedArtifactsFactory;
import org.overlord.sramp.repository.PersistenceFactory;
import org.overlord.sramp.repository.PersistenceManager;
import org.overlord.sramp.repository.QueryManager;
import org.overlord.sramp.repository.QueryManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author <a href="mailto:kurt.stam@gmail.com">Kurt Stam</a>
 */
public abstract class AbstractJCRPersistenceTest {

    protected Logger log = LoggerFactory.getLogger(this.getClass());
    protected static PersistenceManager persistenceManager = null;
    protected static DerivedArtifacts derivedArtifacts = null;
    protected static QueryManager queryManager = null;
    protected static AuditManager auditManager = null;

    @BeforeClass
    public static void setupPersistence() {
		// use the in-memory config for unit tests
		System.setProperty("sramp.modeshape.config.url", "classpath://" + AbstractJCRPersistenceTest.class.getName()
				+ "/META-INF/modeshape-configs/junit-sramp-config.json");

        persistenceManager = PersistenceFactory.newInstance();
        derivedArtifacts = DerivedArtifactsFactory.newInstance();
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
    }

}
