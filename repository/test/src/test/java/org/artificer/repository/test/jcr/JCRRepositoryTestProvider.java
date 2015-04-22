/*
 * Copyright 2014 JBoss Inc
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
package org.artificer.repository.test.jcr;

import org.artificer.repository.PersistenceManager;
import org.artificer.repository.test.RepositoryTestProvider;

/**
 * @author Brett Meyer.
 */
public class JCRRepositoryTestProvider implements RepositoryTestProvider {

    PersistenceManager persistenceManager = null;

    @Override
    public void before() throws Exception {
        System.setProperty("artificer.modeshape.config.url", "classpath://" + JCRRepositoryTestProvider.class.getName()
                + "/META-INF/modeshape-configs/junit-artificer-config.json");

        new JCRRepositoryCleaner().clean();
    }

    @Override
    public void after() throws Exception {

    }
}
