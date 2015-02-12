/*
 * Copyright 2012 JBoss Inc
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
package org.artificer.repository.jcr.mapper;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.artificer.repository.jcr.JCRConstants;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.StoredQuery;

/**
 * @author Brett Meyer
 */
public final class StoredQueryToJCRNode {

    public void write(StoredQuery storedQuery, Node jcrNode) throws RepositoryException {
        jcrNode.setProperty(JCRConstants.SRAMP_QUERY_NAME, storedQuery.getQueryName());
        jcrNode.setProperty(JCRConstants.SRAMP_QUERY_EXPRESSION, storedQuery.getQueryExpression());
        jcrNode.setProperty(JCRConstants.SRAMP_PROPERTY_NAME,
                storedQuery.getPropertyName().toArray(new String[storedQuery.getPropertyName().size()]));
    }
}
