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
import org.overlord.sramp.common.SrampConstants;


/**
 * @author <a href="mailto:kurt.stam@gmail.com">Kurt Stam</a>
 */
public abstract class AbstractNoAuditingJCRPersistenceTest extends AbstractJCRPersistenceTest {

    @BeforeClass
    public static void beforeClass() {
	    System.setProperty(SrampConstants.SRAMP_CONFIG_AUDITING, "false"); //$NON-NLS-1$
        setupPersistence();
    }

    @Override
    @Before
    public void prepForTest() {
        new JCRRepositoryCleaner().clean();
    }

    @AfterClass
    public static void cleanup() {
        persistenceManager.shutdown();
        System.clearProperty(SrampConstants.SRAMP_CONFIG_AUDITING);
    }

}
