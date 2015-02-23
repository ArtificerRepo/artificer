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
package org.artificer.repository.jcr.util;

import org.modeshape.jcr.api.Workspace;
import org.modeshape.jcr.api.index.IndexColumnDefinitionTemplate;
import org.modeshape.jcr.api.index.IndexDefinition;
import org.modeshape.jcr.api.index.IndexDefinitionTemplate;
import org.modeshape.jcr.api.index.IndexManager;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * @author Brett Meyer.
 */
public class JCRQueryIndexUtil {

    public static void createQueryIndex(Session session, String name, boolean isUnique,
            String typeName, String propertyName, int propertyType) throws RepositoryException {
        Workspace workspace = (Workspace) session.getWorkspace();
        IndexManager indexManager = workspace.getIndexManager();

        IndexDefinitionTemplate indexDefinitionTemplate = indexManager.createIndexDefinitionTemplate();
        indexDefinitionTemplate.setName(name);
        indexDefinitionTemplate.setProviderName("local"); // defined in standalone.xml
        if (isUnique) {
            indexDefinitionTemplate.setKind(IndexDefinition.IndexKind.UNIQUE_VALUE);
        } else {
            indexDefinitionTemplate.setKind(IndexDefinition.IndexKind.VALUE);
        }
        indexDefinitionTemplate.setSynchronous(true);
        indexDefinitionTemplate.setNodeTypeName(typeName);
        IndexColumnDefinitionTemplate indexColumnDefinitionTemplate = indexManager.createIndexColumnDefinitionTemplate();
        indexColumnDefinitionTemplate.setPropertyName(propertyName);
        indexColumnDefinitionTemplate.setColumnType(propertyType);
        indexDefinitionTemplate.setColumnDefinitions(indexColumnDefinitionTemplate);
        indexManager.registerIndex(indexDefinitionTemplate, true);
    }
}
