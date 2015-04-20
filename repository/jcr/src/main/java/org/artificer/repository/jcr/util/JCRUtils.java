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
package org.artificer.repository.jcr.util;

import org.artificer.common.ArtifactType;
import org.artificer.common.error.ArtificerConflictException;
import org.artificer.repository.jcr.JCRConstants;
import org.artificer.repository.jcr.MapToJCRPath;
import org.artificer.repository.jcr.i18n.Messages;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.QueryResult;
import javax.jcr.version.VersionException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Some common utils for working with JCR.
 *
 * @author eric.wittmann@redhat.com
 */
public class JCRUtils {

    /**
     * Called to set the jcr:mimeType property on the given artifact.  The mime type property
     * must be set on the nt:resource node, which is a child of the given artifact node.
     * @param artifactNode
     * @param mimeType
     * @throws RepositoryException
     * @throws ConstraintViolationException
     * @throws LockException
     * @throws VersionException
     * @throws ValueFormatException
     */
    public static void setArtifactContentMimeType(Node artifactNode, String mimeType)
            throws ValueFormatException, VersionException, LockException, ConstraintViolationException,
            RepositoryException {
        Node resourceNode = artifactNode.getNode(JCRConstants.JCR_CONTENT);
        resourceNode.setProperty(JCRConstants.JCR_MIME_TYPE, mimeType);
    }

    /**
     * Get or create a node at the specified path.
     *
     * @param parentNode the parent node. may not be null
     * @param path the path of the desired child node. may not be null
     * @param defaultNodeType the default node type. may be null
     * @param finalNodeType the optional final node type. may be null
     * @return the existing or newly created node
     * @throws RepositoryException
     * @throws IllegalArgumentException if either the parentNode or path argument is null
     */
    public static Node findOrCreateNode( Node parentNode,
            String path,
            String defaultNodeType,
            String finalNodeType ) throws RepositoryException {
        isNotNull(parentNode, "parentNode");
        isNotNull(path, "path");
        // Remove leading and trailing slashes ...
        String relPath = path.replaceAll("^/+", "").replaceAll("/+$", "");

        // Look for the node first ...
        try {
            return parentNode.getNode(relPath);
        } catch (PathNotFoundException e) {
            // continue
        }
        // Create the node
        return createNode(parentNode, path, defaultNodeType, finalNodeType);
    }

    /**
     * Create a node at the specified path.
     *
     * @param parentNode the parent node. may not be null
     * @param path the path of the desired child node. may not be null
     * @param defaultNodeType the default node type. may be null
     * @param finalNodeType the optional final node type. may be null
     * @return the newly created node
     * @throws RepositoryException
     * @throws IllegalArgumentException if either the parentNode or path argument is null
     */
    public static Node createNode( Node parentNode,
            String path,
            String defaultNodeType,
            String finalNodeType ) throws RepositoryException {
        isNotNull(parentNode, "parentNode");
        isNotNull(path, "path");
        // Remove leading and trailing slashes ...
        String relPath = path.replaceAll("^/+", "").replaceAll("/+$", "");

        // Create the node, which has to be done segment by segment ...
        String[] pathSegments = relPath.split("/");
        Node node = parentNode;
        for (int i = 0, len = pathSegments.length; i != len; ++i) {
            String pathSegment = pathSegments[i];
            pathSegment = pathSegment.trim();
            if (pathSegment.length() == 0) continue;
            // The 'i < len - 1' is a bit of optimization.  Since we're purely *creating*, we know that the last segment
            // must never exist.
            if (i < len - 1 && node.hasNode(pathSegment)) {
                // Find the existing node ...
                node = node.getNode(pathSegment);
            } else {
                // Make sure there is no index on the final segment ...
                String pathSegmentWithNoIndex = pathSegment.replaceAll("(\\[\\d+\\])+$", "");
                // Create the node ...
                String nodeType = defaultNodeType;
                if (i == len - 1 && finalNodeType != null) nodeType = finalNodeType;
                if (nodeType != null) {
                    node = node.addNode(pathSegmentWithNoIndex, nodeType);
                } else {
                    node = node.addNode(pathSegmentWithNoIndex);
                }
            }
        }
        return node;
    }
    /**
     * Get or create a node with the specified node and node type under the specified parent node.
     *
     * @param parent the parent node. may not be null
     * @param name the name of the child node. may not be null
     * @param nodeType the node type. may be null
     * @return the existing or newly created child node
     * @throws RepositoryException
     */
    public static Node findOrCreateChild( Node parent,
            String name,
            String nodeType ) throws RepositoryException {
        return findOrCreateNode(parent, name, nodeType, nodeType);
    }
    /**
     * Get or create a node at the specified path and node type.
     *
     * @param session the JCR session. may not be null
     * @param path the path of the desired node to be found or created. may not be null
     * @param nodeType the node type. may be null
     * @return the existing or newly created node
     * @throws RepositoryException
     * @throws IllegalArgumentException if either the session or path argument is null
     */
    public static Node findOrCreateNode( Session session,
            String path,
            String nodeType ) throws RepositoryException {
        return findOrCreateNode(session, path, nodeType, nodeType);
    }
    /**
     * Get or create a node at the specified path.
     *
     * @param session the JCR session. may not be null
     * @param path the path of the desired node to be found or created. may not be null
     * @param defaultNodeType the default node type. may be null
     * @param finalNodeType the optional final node type. may be null
     * @return the existing or newly created node
     * @throws RepositoryException
     * @throws IllegalArgumentException if either the session or path argument is null
     */
    public static Node findOrCreateNode( Session session,
            String path,
            String defaultNodeType,
            String finalNodeType ) throws RepositoryException {
        isNotNull(session, "session");
        Node root = session.getRootNode();
        return findOrCreateNode(root, path, defaultNodeType, finalNodeType);
    }
    /**
     * Upload the content in the supplied stream into the repository at the defined path, using the given session. This method
     * will create a 'nt:file' node at the supplied path, and any non-existant ancestors with nodes of type 'nt:folder'. As
     * defined by the JCR specification, the binary content (and other properties) will be placed on a child of the 'nt:file' node
     * named 'jcr:content' with a node type of 'nt:resource'.
     * <p>
     * This method always closes the supplied stream.
     * </p>
     *
     * @param session the JCR session
     * @param path the path to the file
     * @param stream the stream containing the content to be uploaded
     * @param isUpdate whether or not the upload action is an update (vs. a creation)
     * @return the newly created 'nt:file' node
     * @throws RepositoryException if there is a problem uploading the file
     * @throws IOException if there is a problem using the stream
     * @throws IllegalArgumentException is any of the parameters are null
     */
    public static Node uploadFile( Session session,
            String path,
            InputStream stream,
            boolean isUpdate) throws RepositoryException, IOException {
        isNotNull(session, "session");
        isNotNull(path, "path");
        Node fileNode = null;
        boolean error = false;
        try {
            // Create an 'nt:file' node at the supplied path, creating any missing intermediate nodes of type 'nt:folder' ...
            Node contentNode;
            if (isUpdate) {
                // May already exist -- find, then create
                fileNode = findOrCreateNode(session.getRootNode(), path, JCRConstants.NT_FOLDER, JCRConstants.NT_FILE);
                contentNode = findOrCreateChild(fileNode, JCRConstants.JCR_CONTENT, JCRConstants.NT_RESOURCE);
            } else {
                // Shouldn't exist -- create
                fileNode = createNode(session.getRootNode(), path, JCRConstants.NT_FOLDER, JCRConstants.NT_FILE);
                contentNode = createNode(fileNode, JCRConstants.JCR_CONTENT, JCRConstants.NT_RESOURCE, JCRConstants.NT_RESOURCE);
            }

            Binary binary = session.getValueFactory().createBinary(stream);
            contentNode.setProperty(JCRConstants.JCR_DATA, binary);
        } catch (RepositoryException e) {
            error = true;
            throw e;
        } catch (RuntimeException e) {
            error = true;
            throw e;
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (RuntimeException e) {
                if (!error) throw e; // don't override any exception thrown in the block above
            }
        }
        return fileNode;
    }

    private static void isNotNull( Object argument, String name ) {
        if (argument == null) {
            throw new IllegalArgumentException(Messages.i18n.format("ARG_CANNOT_BE_NULL", name));
        }
    }

    /**
     * Load the subgraph below this node, and print it to System.out if printing is enabled.
     *
     * @param node the root of the subgraph
     * @throws RepositoryException
     */
    public static void printSubgraph( Node node ) throws RepositoryException {
        printSubgraph(node, Integer.MAX_VALUE);
    }

    /**
     * Load the subgraph below this node, and print it to System.out if printing is enabled.
     *
     * @param node the root of the subgraph
     * @param maxDepth the maximum depth of the subgraph that should be printed
     * @throws RepositoryException
     */
    public static void printSubgraph( Node node,
            int maxDepth ) throws RepositoryException {
        printSubgraph(node, " ", node.getDepth(), maxDepth);
    }

    /**
     * Print this node and its properties to System.out if printing is enabled.
     *
     * @param node the node to be printed
     * @throws RepositoryException
     */
    public static void printNode( Node node ) throws RepositoryException {
        printSubgraph(node, " ", node.getDepth(), 1);
    }

    /**
     * Load the subgraph below this node, and print it to System.out if printing is enabled.
     *
     * @param node the root of the subgraph
     * @param lead the string that each line should begin with; may be null if there is no such string
     * @param depthOfSubgraph the depth of this subgraph's root node
     * @param maxDepthOfSubgraph the maximum depth of the subgraph that should be printed
     * @throws RepositoryException
     */
    public static void printSubgraph( Node node,
            String lead,
            int depthOfSubgraph,
            int maxDepthOfSubgraph ) throws RepositoryException {
        int currentDepth = node.getDepth() - depthOfSubgraph + 1;
        if (currentDepth > maxDepthOfSubgraph) return;
        if (lead == null) lead = "";
        String nodeLead = lead + createString(' ', (currentDepth - 1) * 2);

        StringBuilder sb = new StringBuilder();
        sb.append(nodeLead);
        if (node.getDepth() == 0) {
            sb.append("/");
        } else {
            sb.append(node.getName());
            if (node.getIndex() != 1) {
                sb.append('[').append(node.getIndex()).append(']');
            }
        }
        sb.append(" " + JCRConstants.JCR_PRIMARY_TYPE + "=" + node.getPrimaryNodeType().getName());
        boolean referenceable = node.isNodeType("mix:referenceable");
        if (node.getMixinNodeTypes().length != 0) {
            sb.append(" " + JCRConstants.JCR_MIXIN_TYPES + "=[");
            boolean first = true;
            for (NodeType mixin : node.getMixinNodeTypes()) {
                if (first) first = false;
                else sb.append(',');
                sb.append(mixin.getName());
            }
            sb.append(']');
        }
        if (referenceable) {
            sb.append(" " + JCRConstants.JCR_UUID + "=" + node.getIdentifier());
        }
        System.out.println(sb);

        List<String> propertyNames = new LinkedList<String>();
        for (PropertyIterator iter = node.getProperties(); iter.hasNext();) {
            Property property = iter.nextProperty();
            String name = property.getName();
            if (name.equals(JCRConstants.JCR_PRIMARY_TYPE) || name.equals(JCRConstants.JCR_MIXIN_TYPES) || name.equals(JCRConstants.JCR_UUID)) continue;
            propertyNames.add(property.getName());
        }
        Collections.sort(propertyNames);
        for (String propertyName : propertyNames) {
            Property property = node.getProperty(propertyName);
            sb = new StringBuilder();
            sb.append(nodeLead).append("  - ").append(propertyName).append('=');
            int type = property.getType();
            boolean binary = type == PropertyType.BINARY;
            if (property.isMultiple()) {
                sb.append('[');
                boolean first = true;
                for (Value value : property.getValues()) {
                    if (first) first = false;
                    else sb.append(',');
                    if (binary) {
                        sb.append(value.getBinary());
                    } else {
                        sb.append(getStringValue(value, type));
                    }
                }
                sb.append(']');
            } else {
                Value value = property.getValue();
                if (binary) {
                    sb.append(value.getBinary());
                } else {
                    sb.append(getStringValue(value, type));
                }
            }
            System.out.println(sb);
        }

        if (currentDepth < maxDepthOfSubgraph) {
            for (NodeIterator iter = node.getNodes(); iter.hasNext();) {
                Node child = iter.nextNode();
                printSubgraph(child, lead, depthOfSubgraph, maxDepthOfSubgraph);
            }
        }
    }

    public static Node findNode(String path, Session session) throws Exception {
        try {
            return session.getNode(path);
        } catch (PathNotFoundException e) {
            return null;
        }
    }

    /**
     * Finds the JCR node for the given artifact (UUID + type).
     * @param uuid
     * @param type
     * @param session
     * @throws Exception
     */
    public static Node findArtifactNode(String uuid, ArtifactType type, Session session) throws Exception {
        if (type.getArtifactType().isDerived() || type.isExtendedType()) {
            // Since we don't know the derived artifact's parent, we have to query by UUID.
            // Also, if it's extended, we won't know whether or not it's derived ahead of time.  To be safe, simply
            // query by uuid.
            return findArtifactNodeByUuid(session, uuid);
        } else {
            try {
                // Optimization.  We know it's a primary, so simply use the path.
                return session.getNode(MapToJCRPath.getArtifactPath(uuid));
            } catch (PathNotFoundException e) {
                return null;
            }
        }
    }

    /**
     * Utility method to find an s-ramp artifact node by its UUID.  Returns null if
     * not found.  Throws an exception if too many JCR nodes are found with the given
     * UUID.
     * @param session
     * @param artifactUuid
     * @throws Exception
     */
    public static Node findArtifactNodeByUuid(Session session, String artifactUuid) throws Exception {
        javax.jcr.query.QueryManager jcrQueryManager = session.getWorkspace().getQueryManager();
        String jcrSql2Query = String.format("SELECT * FROM [sramp:baseArtifactType] WHERE [sramp:uuid] = '%1$s'", artifactUuid);
        jcrSql2Query += JCRConstants.NOT_DELETED_FILTER;
        javax.jcr.query.Query jcrQuery = jcrQueryManager.createQuery(jcrSql2Query, JCRConstants.JCR_SQL2);
        QueryResult jcrQueryResult = jcrQuery.execute();
        NodeIterator jcrNodes = jcrQueryResult.getNodes();
        if (!jcrNodes.hasNext()) {
            return null;
        }
        if (jcrNodes.getSize() > 1) {
            throw new Exception(Messages.i18n.format("TOO_MANY_ARTIFACTS", artifactUuid));
        }
        Node node = jcrNodes.nextNode();
        return node;
    }

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
                        // targets the primary artifact or any of its derived artifacts
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
            throw ArtificerConflictException.relationshipConstraint(uuid);
        }
    }

    /**
     * Finds any relationships targeting the given artifact UUID.
     *
     * @param targetedUuid
     * @param session
     * @return NodeIterator
     * @throws Exception
     */
    public static NodeIterator reverseRelationships(String targetedUuid, Session session) throws Exception {
        Node targetedNode = findArtifactNodeByUuid(session, targetedUuid);
        String query = String.format("SELECT r.* FROM [sramp:relationship] AS r " +
                        "JOIN [sramp:target] AS t ON ISCHILDNODE(t, r) " +
                        // root path, *not* in the trash
                        "WHERE ISDESCENDANTNODE(r, '" + JCRConstants.ROOT_PATH + "') " +
                        // targets the primary artifact
                        "AND REFERENCE(t) = '%1$s'",
                targetedNode.getIdentifier());
        javax.jcr.query.QueryManager jcrQueryManager = session.getWorkspace().getQueryManager();
        javax.jcr.query.Query jcrQuery = jcrQueryManager.createQuery(query, JCRConstants.JCR_SQL2);
        QueryResult jcrQueryResult = jcrQuery.execute();
        return jcrQueryResult.getNodes();
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
                throw ArtificerConflictException.customPropertyConstraint(uuid);
            }

            // Does the Node have classifiers?
            if (childNode.hasProperty(JCRConstants.SRAMP_CLASSIFIED_BY)
                    && childNode.getProperty(JCRConstants.SRAMP_CLASSIFIED_BY).getValues().length > 0) {
                throw ArtificerConflictException.classifierConstraint(uuid);
            }
            if (childNode.hasProperty(JCRConstants.SRAMP_NORMALIZED_CLASSIFIED_BY)
                    && childNode.getProperty(JCRConstants.SRAMP_NORMALIZED_CLASSIFIED_BY).getValues().length > 0) {
                throw ArtificerConflictException.classifierConstraint(uuid);
            }
        }
    }

//    /**
//     * Deletes all derived relationship nodes that point *to* the given artifact or its derived artifacts
//     *
//     * @param primaryNode
//     * @param session
//     * @throws Exception
//     */
//    public static void deleteDerivedReverseRelationships(Node primaryNode, Session session) throws Exception {
//        String query = String.format("SELECT r.* FROM [sramp:relationship] AS r " +
//                        "JOIN [sramp:target] AS t ON ISCHILDNODE(t, r) " +
//                        // root path, *not* in the trash
//                        "WHERE ISDESCENDANTNODE(r, '" + JCRConstants.ROOT_PATH + "') " +
//                        // relationship is not from one of the given artifact's children
//                        "AND NOT(ISDESCENDANTNODE(r, '%2$s')) " +
//                        // derived relationships only
//                        "AND r.[sramp:derived] = true " +
//                        // targets the primary artifact or any of its derived artifacts
//                        "AND (REFERENCE(t) = '%1$s' OR REFERENCE(t) IN (SELECT referenced.[jcr:uuid] FROM [sramp:baseArtifactType] AS referenced WHERE ISDESCENDANTNODE(referenced, '%2$s')))",
//                primaryNode.getIdentifier(), primaryNode.getPath());
//        javax.jcr.query.QueryManager jcrQueryManager = session.getWorkspace().getQueryManager();
//        javax.jcr.query.Query jcrQuery = jcrQueryManager.createQuery(query, JCRConstants.JCR_SQL2);
//        QueryResult jcrQueryResult = jcrQuery.execute();
//        NodeIterator jcrNodes = jcrQueryResult.getNodes();
//
//        while (jcrNodes.hasNext()) {
//            Node node = jcrNodes.nextNode();
//            // delete it
//            node.remove();
//        }
//
//        session.save();
//    }

    /**
     * Deletes *all* relationship nodes that point *to* the given artifact or its derived artifacts.  Primarily used
     * for force deletes.
     *
     * @param primaryNode
     * @param session
     * @throws Exception
     */
    public static void deleteReverseRelationships(Node primaryNode, Session session) throws Exception {
        String query = String.format("SELECT r.* FROM [sramp:relationship] AS r " +
                        "JOIN [sramp:target] AS t ON ISCHILDNODE(t, r) " +
                        // root path, *not* in the trash
                        "WHERE ISDESCENDANTNODE(r, '" + JCRConstants.ROOT_PATH + "') " +
                        // relationship is not from one of the given artifact's children
                        "AND NOT(ISDESCENDANTNODE(r, '%2$s')) " +
                        // targets the primary artifact or any of its derived artifacts
                        "AND (REFERENCE(t) = '%1$s' OR REFERENCE(t) IN (SELECT referenced.[jcr:uuid] FROM [sramp:baseArtifactType] AS referenced WHERE ISDESCENDANTNODE(referenced, '%2$s')))",
                primaryNode.getIdentifier(), primaryNode.getPath());
        javax.jcr.query.QueryManager jcrQueryManager = session.getWorkspace().getQueryManager();
        javax.jcr.query.Query jcrQuery = jcrQueryManager.createQuery(query, JCRConstants.JCR_SQL2);
        QueryResult jcrQueryResult = jcrQuery.execute();
        NodeIterator jcrNodes = jcrQueryResult.getNodes();

        while (jcrNodes.hasNext()) {
            Node node = jcrNodes.nextNode();
            // delete it
            node.remove();
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

    private static String createString( final char charToRepeat,
            int numberOfRepeats ) {
        assert numberOfRepeats >= 0;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numberOfRepeats; ++i) {
            sb.append(charToRepeat);
        }
        return sb.toString();
    }

    private static String getStringValue( Value value,
            int type ) throws RepositoryException {
        String result = value.getString();
        if (type == PropertyType.STRING) {
            result = "\"" + result + "\"";
        }
        return result;
    }


}
