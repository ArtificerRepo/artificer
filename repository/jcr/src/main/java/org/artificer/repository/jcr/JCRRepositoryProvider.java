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
package org.artificer.repository.jcr;

import org.artificer.repository.AuditManager;
import org.artificer.repository.PersistenceManager;
import org.artificer.repository.QueryManager;
import org.artificer.repository.RepositoryProvider;

/**
 * @author Brett Meyer.
 */
public class JCRRepositoryProvider implements RepositoryProvider {

    private PersistenceManager persistenceManager = new JCRPersistence();

    private QueryManager queryManager = new JCRQueryManager();

    private AuditManager auditManager = new JCRAuditManager();

    @Override
    public String name() {
        return "jcr";
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
}
