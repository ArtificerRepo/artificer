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
package org.artificer.repository.jcr;

import org.artificer.common.ArtificerException;
import org.artificer.common.ReverseRelationship;
import org.artificer.common.error.ArtificerServerException;
import org.artificer.repository.QueryManager;
import org.artificer.repository.jcr.query.JCRArtificerQuery;
import org.artificer.repository.jcr.util.JCRUtils;
import org.artificer.repository.query.ArtificerQuery;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of the {@link QueryManager} using JCR.  Works along with the
 * JCR PersistenceManager implementation ({@link JCRPersistence}).
 *
 * @author eric.wittmann@redhat.com
 */
public class JCRQueryManager implements QueryManager {

    @Override
    public void login(String username, String password) {
        JCRRepositoryFactory.setLoginCredentials(username, password);
    }

	@Override
	public ArtificerQuery createQuery(String xpathTemplate, String orderByProperty, boolean orderAscending) {
		return new JCRArtificerQuery(xpathTemplate, orderByProperty, orderAscending);
	}

	@Override
	public ArtificerQuery createQuery(String xpathTemplate) {
		return createQuery(xpathTemplate, null, false);
	}

    @Override
    public List<ReverseRelationship> reverseRelationships(String uuid) throws ArtificerException {
        Session session = null;
        try {
            session = JCRRepositoryFactory.getSession();
            NodeIterator relationshipNodes = JCRUtils.reverseRelationships(uuid, session);
            List<ReverseRelationship> relationships = new ArrayList<ReverseRelationship>();
            while (relationshipNodes.hasNext()) {
                Node relationshipNode = relationshipNodes.nextNode();
                String relationshipType = relationshipNode.getProperty(JCRConstants.SRAMP_RELATIONSHIP_TYPE).getString();
                boolean generic = relationshipNode.getProperty(JCRConstants.SRAMP_GENERIC).getBoolean();
                Node artifactNode = relationshipNode.getParent();
                BaseArtifactType artifact = JCRNodeToArtifactFactory.createArtifact(session, artifactNode);
                ReverseRelationship relationship = new ReverseRelationship(relationshipType, artifact, generic);
                relationships.add(relationship);
            }
            return relationships;
        } catch (ArtificerException se) {
            throw se;
        } catch (Throwable t) {
            throw new ArtificerServerException(t);
        } finally {
            JCRRepositoryFactory.logoutQuietly(session);
        }
    }

}
