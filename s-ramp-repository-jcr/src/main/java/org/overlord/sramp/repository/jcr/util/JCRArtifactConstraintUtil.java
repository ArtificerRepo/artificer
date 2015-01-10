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
package org.overlord.sramp.repository.jcr.util;

import org.overlord.sramp.common.ClassifierConstraintException;
import org.overlord.sramp.common.CustomPropertyConstraintException;
import org.overlord.sramp.common.RelationshipConstraintException;
import org.overlord.sramp.common.visitors.HierarchicalArtifactVisitor;
import org.overlord.sramp.repository.jcr.JCRConstants;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PropertyIterator;
import javax.jcr.Session;
import javax.jcr.query.QueryResult;

/**
 * Provides constraint checks on relationships, custom properties, etc., useful for update/delete actions.
 *
 * @author Brett Meyer.
 */
public class JCRArtifactConstraintUtil extends HierarchicalArtifactVisitor {

    /**
     * Finds all generic and non-derived modeled relationship nodes that point *to* the given artifact.  Also finds
     * relationships pointing to the given artifact's derived artifacts.  If any are found, throws RelationshipConstraintException
     *
     * @param uuid
     * @param primaryNode
     * @param session
     * @throws Exception
     */
    public static void relationshipConstraints(String uuid, Node primaryNode, Session session) throws Exception {
        String query = String.format("SELECT r.* FROM [sramp:relationship] AS r " +
                        "JOIN [sramp:target] AS t ON ISCHILDNODE(t, r) " +
                        // root path, *not* in the trash
                        "WHERE ISDESCENDANTNODE(r, '" + JCRConstants.ROOT_PATH + "') " +
                        // relationship is not from one of the given artifact's children
                        "AND NOT(ISDESCENDANTNODE(r, '%2$s')) " +
                        // only generic or modeled, but not derived
                        "AND (r.[sramp:generic] = true OR r.[sramp:derived] = false) " +
                        // targets any of the primary artifact's derived artifacts
                        "AND (REFERENCE(t) = '%1$s' OR REFERENCE(t) IN (SELECT referenced.[jcr:uuid] FROM [sramp:baseArtifactType] AS referenced WHERE ISDESCENDANTNODE(referenced, '%2$s')))",
                primaryNode.getIdentifier(), primaryNode.getPath());
        relationshipConstraints(uuid, query, session);
    }

    /**
     * Finds relationships pointing to the given artifact's derived artifacts.
     * If any are found, throws RelationshipConstraintException
     *
     * @param uuid
     * @param primaryNode
     * @param session
     * @throws Exception
     */
    public static void relationshipConstraintsOnDerived(String uuid, Node primaryNode, Session session) throws Exception {
        String query = String.format("SELECT r.* FROM [sramp:relationship] AS r " +
                        "JOIN [sramp:target] AS t ON ISCHILDNODE(t, r) " +
                        // root path, *not* in the trash
                        "WHERE ISDESCENDANTNODE(r, '" + JCRConstants.ROOT_PATH + "') " +
                        // relationship is not from one of the given artifact's children
                        "AND NOT(ISDESCENDANTNODE(r, '%2$s')) " +
                        // only generic or modeled, but not derived
                        "AND (r.[sramp:generic] = true OR r.[sramp:derived] = false) " +
                        // targets any of the primary artifact's derived artifacts
                        "AND REFERENCE(t) IN (SELECT referenced.[jcr:uuid] FROM [sramp:baseArtifactType] AS referenced WHERE ISDESCENDANTNODE(referenced, '%2$s'))",
                primaryNode.getIdentifier(), primaryNode.getPath());
        relationshipConstraints(uuid, query, session);
    }

    private static void relationshipConstraints(String uuid, String query, Session session) throws Exception {
        javax.jcr.query.QueryManager jcrQueryManager = session.getWorkspace().getQueryManager();
        javax.jcr.query.Query jcrQuery = jcrQueryManager.createQuery(query, JCRConstants.JCR_SQL2);
        QueryResult jcrQueryResult = jcrQuery.execute();
        NodeIterator jcrNodes = jcrQueryResult.getNodes();

        if (jcrNodes.hasNext()) {
            throw new RelationshipConstraintException(uuid);
        }
    }

    /**
     * If the given artifact's *derived artifacts* have any custom properties or classifiers attached, throw a
     * CustomPropertyConstraintException or ClassifierConstraintException (respectively).
     *
     * @param uuid
     * @param primaryNode
     * @throws Exception
     */
    public static void customMetadataConstraintsOnDerived(String uuid, Node primaryNode) throws Exception {
        NodeIterator childNodes = primaryNode.getNodes();
        while (childNodes.hasNext()) {
            Node childNode = childNodes.nextNode();

            // Does the Node have custom properties?
            PropertyIterator customProperties = childNode.getProperties(JCRConstants.SRAMP_PROPERTIES + ":*");
            if (customProperties.hasNext()) {
                throw new CustomPropertyConstraintException(uuid);
            }

            // Does the Node have classifiers?
            if (childNode.hasProperty(JCRConstants.SRAMP_CLASSIFIED_BY)
                    && childNode.getProperty(JCRConstants.SRAMP_CLASSIFIED_BY).getValues().length > 0) {
                throw new ClassifierConstraintException(uuid);
            }
            if (childNode.hasProperty(JCRConstants.SRAMP_NORMALIZED_CLASSIFIED_BY)
                    && childNode.getProperty(JCRConstants.SRAMP_NORMALIZED_CLASSIFIED_BY).getValues().length > 0) {
                throw new ClassifierConstraintException(uuid);
            }
        }
    }

    /**
     * Deletes all derived relationship nodes that point *to* the given artifact or its derived artifacts
     *
     * @param primaryNode
     * @param session
     * @throws Exception
     */
    public static void deleteDerivedRelationships(Node primaryNode, Session session) throws Exception {
        String query = String.format("SELECT r.* FROM [sramp:relationship] AS r " +
                "JOIN [sramp:target] AS t ON ISCHILDNODE(t, r) " +
                // root path, *not* in the trash
                "WHERE ISDESCENDANTNODE(r, '" + JCRConstants.ROOT_PATH + "') " +
                // relationship is not from one of the given artifact's children
                "AND NOT(ISDESCENDANTNODE(r, '%2$s')) " +
                // derived relationships only
                "AND r.[sramp:derived] = true " +
                // targets the primary artifact or any of its derived artifacts
                "AND (REFERENCE(t) = '%1$s' OR REFERENCE(t) IN (SELECT referenced.[jcr:uuid] FROM [sramp:baseArtifactType] AS referenced WHERE ISDESCENDANTNODE(referenced, '%2$s')))",
                primaryNode.getIdentifier(), primaryNode.getPath());
        javax.jcr.query.QueryManager jcrQueryManager = session.getWorkspace().getQueryManager();
        javax.jcr.query.Query jcrQuery = jcrQueryManager.createQuery(query, JCRConstants.JCR_SQL2);
        QueryResult jcrQueryResult = jcrQuery.execute();
        NodeIterator jcrNodes = jcrQueryResult.getNodes();

        while (jcrNodes.hasNext()) {
            // delete it
            jcrNodes.nextNode().remove();
        }

        session.save();
    }

    /**
     * Deletes all derived artifact nodes from the given primary artifact
     *
     * @param primaryNode
     * @param session
     * @throws Exception
     */
    public static void deleteDerivedArtifacts(Node primaryNode, Session session) throws Exception {
        // Delete all of the primary artifact's relationships that target one of its derived artifacts.
        String query = String.format("SELECT r.* FROM [sramp:relationship] AS r " +
                "JOIN [sramp:target] AS t ON ISCHILDNODE(t, r) " +
                "WHERE ISDESCENDANTNODE(r, '%1$s') " +
                "AND REFERENCE(t) IN (SELECT referenced.[jcr:uuid] FROM [sramp:derivedArtifactType] AS referenced WHERE ISDESCENDANTNODE(referenced, '%1$s'))",
                primaryNode.getPath());
        javax.jcr.query.QueryManager jcrQueryManager = session.getWorkspace().getQueryManager();
        javax.jcr.query.Query jcrQuery = jcrQueryManager.createQuery(query, JCRConstants.JCR_SQL2);
        QueryResult jcrQueryResult = jcrQuery.execute();
        NodeIterator jcrNodes = jcrQueryResult.getNodes();
        while (jcrNodes.hasNext()) {
            // delete it
            jcrNodes.nextNode().remove();
        }

        // Necessary to save, prior to the next step, in order to prevent a ReferentialIntegrityException
        session.save();

        // Delete all derived artifacts that descend from the primary
        query = String.format("SELECT * FROM [sramp:derivedArtifactType] WHERE ISDESCENDANTNODE('%1$s')",
                primaryNode.getPath());
        jcrQuery = jcrQueryManager.createQuery(query, JCRConstants.JCR_SQL2);
        jcrQueryResult = jcrQuery.execute();
        jcrNodes = jcrQueryResult.getNodes();
        while (jcrNodes.hasNext()) {
            // delete it
            jcrNodes.nextNode().remove();
        }

        session.save();
    }
}
