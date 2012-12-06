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
package org.overlord.sramp.repository.jcr.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

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
import javax.jcr.version.VersionException;

import org.overlord.sramp.repository.jcr.JCRConstants;

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
    public static void setArtifactContentMimeType(Node artifactNode, String mimeType) throws ValueFormatException,
    VersionException, LockException, ConstraintViolationException, RepositoryException {
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
    public Node findOrCreateNode( Node parentNode,
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
        // Create the node, which has to be done segment by segment ...
        String[] pathSegments = relPath.split("/");
        Node node = parentNode;
        for (int i = 0, len = pathSegments.length; i != len; ++i) {
            String pathSegment = pathSegments[i];
            pathSegment = pathSegment.trim();
            if (pathSegment.length() == 0) continue;
            if (node.hasNode(pathSegment)) {
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
    public Node findOrCreateChild( Node parent,
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
    public Node findOrCreateNode( Session session,
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
    public Node findOrCreateNode( Session session,
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
     * @return the newly created 'nt:file' node
     * @throws RepositoryException if there is a problem uploading the file
     * @throws IOException if there is a problem using the stream
     * @throws IllegalArgumentException is any of the parameters are null
     */
    public Node uploadFile( Session session,
            String path,
            InputStream stream ) throws RepositoryException, IOException {
        isNotNull(session, "session");
        isNotNull(path, "path");
        isNotNull(stream, "stream");
        Node fileNode = null;
        boolean error = false;
        try {
            // Create an 'nt:file' node at the supplied path, creating any missing intermediate nodes of type 'nt:folder' ...
            fileNode = findOrCreateNode(session.getRootNode(), path, "nt:folder", "nt:file");

            // Upload the file to that node ...
            Node contentNode = findOrCreateChild(fileNode, "jcr:content", "nt:resource");
            Binary binary = session.getValueFactory().createBinary(stream);
            contentNode.setProperty("jcr:data", binary);
        } catch (RepositoryException e) {
            error = true;
            throw e;
        } catch (RuntimeException e) {
            error = true;
            throw e;
        } finally {
            try {
                stream.close();
            } catch (RuntimeException e) {
                if (!error) throw e; // don't override any exception thrown in the block above
            }
        }
        return fileNode;
    }

    private static void isNotNull( Object argument, String name ) {
        if (argument == null) {
            throw new IllegalArgumentException("The argument \"" + name + "\" may not be null");
        }
    }
    
    /**
     * Load the subgraph below this node, and print it to System.out if printing is enabled.
     * 
     * @param node the root of the subgraph
     * @throws RepositoryException
     */
    public void printSubgraph( Node node ) throws RepositoryException {
        printSubgraph(node, Integer.MAX_VALUE);
    }

    /**
     * Load the subgraph below this node, and print it to System.out if printing is enabled.
     * 
     * @param node the root of the subgraph
     * @param maxDepth the maximum depth of the subgraph that should be printed
     * @throws RepositoryException
     */
    public void printSubgraph( Node node,
            int maxDepth ) throws RepositoryException {
        printSubgraph(node, " ", node.getDepth(), maxDepth);
    }

    /**
     * Print this node and its properties to System.out if printing is enabled.
     * 
     * @param node the node to be printed
     * @throws RepositoryException
     */
    public void printNode( Node node ) throws RepositoryException {
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
    public void printSubgraph( Node node,
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
        sb.append(" jcr:primaryType=" + node.getPrimaryNodeType().getName());
        boolean referenceable = node.isNodeType("mix:referenceable");
        if (node.getMixinNodeTypes().length != 0) {
            sb.append(" jcr:mixinTypes=[");
            boolean first = true;
            for (NodeType mixin : node.getMixinNodeTypes()) {
                if (first) first = false;
                else sb.append(',');
                sb.append(mixin.getName());
            }
            sb.append(']');
        }
        if (referenceable) {
            sb.append(" jcr:uuid=" + node.getIdentifier());
        }
        System.out.println(sb);

        List<String> propertyNames = new LinkedList<String>();
        for (PropertyIterator iter = node.getProperties(); iter.hasNext();) {
            Property property = iter.nextProperty();
            String name = property.getName();
            if (name.equals("jcr:primaryType") || name.equals("jcr:mixinTypes") || name.equals("jcr:uuid")) continue;
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

    private static String createString( final char charToRepeat,
            int numberOfRepeats ) {
        assert numberOfRepeats >= 0;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numberOfRepeats; ++i) {
            sb.append(charToRepeat);
        }
        return sb.toString();
    }

    protected String getStringValue( Value value,
            int type ) throws RepositoryException {
        String result = value.getString();
        if (type == PropertyType.STRING) {
            result = "\"" + result + "\"";
        }
        return result;
    }


}
