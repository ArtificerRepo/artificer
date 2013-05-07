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
package org.overlord.sramp.repository.jcr;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.query.QueryResult;

import org.overlord.sramp.common.ArtifactType;

/**
 * Base class for JCR manager.
 */
public class AbstractJCRManager {

//	private static Logger log = LoggerFactory.getLogger(AbstractJCRManager.class);

	/**
	 * Default constructor.
	 */
	public AbstractJCRManager() {
	}

    /**
     * Finds the JCR node for the given artifact (UUID + type).
     * @param uuid
     * @param type
     * @param session
     * @throws Exception
     */
    protected static Node findArtifactNode(String uuid, ArtifactType type, Session session) throws Exception {
        Node artifactNode = null;
        if (type.getArtifactType().isDerived()) {
            artifactNode = findArtifactNodeByUuid(session, uuid);
        } else {
            String artifactPath = MapToJCRPath.getArtifactPath(uuid, type);
            if (session.nodeExists(artifactPath)) {
                artifactNode = session.getNode(artifactPath);
            } else {
                artifactNode = findArtifactNodeByUuid(session, uuid);
            }
        }
        return artifactNode;
    }

    /**
     * Utility method to find an s-ramp artifact node by its UUID.  Returns null if
     * not found.  Throws an exception if too many JCR nodes are found with the given
     * UUID.
     * @param session
     * @param artifactUuid
     * @throws Exception
     */
    protected static Node findArtifactNodeByUuid(Session session, String artifactUuid) throws Exception {
        javax.jcr.query.QueryManager jcrQueryManager = session.getWorkspace().getQueryManager();
        String jcrSql2Query = String.format("SELECT * FROM [sramp:baseArtifactType] WHERE [sramp:uuid] = '%1$s'", artifactUuid);
        javax.jcr.query.Query jcrQuery = jcrQueryManager.createQuery(jcrSql2Query, JCRConstants.JCR_SQL2);
        QueryResult jcrQueryResult = jcrQuery.execute();
        NodeIterator jcrNodes = jcrQueryResult.getNodes();
        if (!jcrNodes.hasNext()) {
            return null;
        }
        if (jcrNodes.getSize() > 1) {
            throw new Exception("Too many artifacts found with UUID: " + artifactUuid);
        }
        Node node = jcrNodes.nextNode();
        return node;
    }

}
