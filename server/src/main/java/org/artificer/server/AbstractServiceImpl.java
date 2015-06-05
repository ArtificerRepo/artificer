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
package org.artificer.server;

import org.apache.commons.lang.StringUtils;
import org.artificer.repository.AuditManager;
import org.artificer.repository.PersistenceManager;
import org.artificer.repository.RepositoryProviderFactory;
import org.artificer.server.core.api.AbstractService;
import org.artificer.repository.QueryManager;

/**
 * @author Brett Meyer.
 */
public abstract class AbstractServiceImpl implements AbstractService {

    private String username;
    private String password;

    @Override
    public void login(String username, String password) {
        this.username = username;
        this.password = password;
    }

    protected PersistenceManager persistenceManager() {
        PersistenceManager persistenceManager = RepositoryProviderFactory.persistenceManager();
        if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
            persistenceManager.login(username, password);
        }
        return persistenceManager;
    }

    protected AuditManager auditManager() {
        AuditManager auditManager = RepositoryProviderFactory.auditManager();
        if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
            auditManager.login(username, password);
        }
        return auditManager;
    }

    protected QueryManager queryManager() {
        QueryManager queryManager = RepositoryProviderFactory.queryManager();
        if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
            queryManager.login(username, password);
        }
        return queryManager;
    }
}
