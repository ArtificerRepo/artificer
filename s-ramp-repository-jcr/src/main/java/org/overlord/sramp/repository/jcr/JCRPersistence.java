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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;

import org.apache.commons.io.IOUtils;
import org.modeshape.jcr.api.JcrTools;
import org.overlord.sramp.ArtifactType;
import org.overlord.sramp.repository.DerivedArtifacts;
import org.overlord.sramp.repository.DerivedArtifactsCreationException;
import org.overlord.sramp.repository.PersistenceManager;
import org.overlord.sramp.repository.RepositoryException;
import org.overlord.sramp.repository.jcr.util.DeleteOnCloseFileInputStream;
import org.overlord.sramp.repository.jcr.util.JCRUtils;
import org.overlord.sramp.visitors.ArtifactVisitorHelper;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.DerivedArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.UserDefinedArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.XmlDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A JCR implementation of both the {@link PersistenceManager} and the {@link DerivedArtifacts}
 * interfaces.  By implementing both of these interfaces, this class provides a JCR backend implementation
 * of the S-RAMP repository.
 *
 * This particular implementation leverages the ModeShape sequencing feature to assist with the
 * creation of the S-RAMP derived artifacts.
 */
public class JCRPersistence implements PersistenceManager, DerivedArtifacts {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Default constructor.
     * @throws RepositoryException
     * @throws IOException
     */
    public JCRPersistence() throws RepositoryException, IOException {
    }

    /**
     * @see org.overlord.sramp.repository.PersistenceManager#persistArtifact(java.lang.String, org.overlord.sramp.ArtifactType, java.io.InputStream)
     */
    @Override
	public BaseArtifactType persistArtifact(String name, ArtifactType artifactType, InputStream content) throws RepositoryException {
        Session session = null;
        try {
            session = JCRRepository.getSession();
            JcrTools tools = new JcrTools();
            String uuid = UUID.randomUUID().toString();
            String artifactPath = MapToJCRPath.getArtifactPath(uuid, artifactType);
            log.debug("Uploading file {} to JCR.",name);

            Node artifactNode = tools.uploadFile(session, artifactPath, content);
            JCRUtils.setArtifactContentMimeType(artifactNode, artifactType.getMimeType());

            String jcrMixinName = artifactType.getArtifactType().getApiType().value();
            jcrMixinName = JCRConstants.SRAMP + jcrMixinName.substring(0,1).toLowerCase() + jcrMixinName.substring(1);
            artifactNode.addMixin(jcrMixinName);
            //BaseArtifactType
            artifactNode.setProperty(JCRConstants.SRAMP_UUID, uuid);
            artifactNode.setProperty(JCRConstants.SRAMP_NAME, name);
            artifactNode.setProperty(JCRConstants.SRAMP_ARTIFACT_MODEL, artifactType.getArtifactType().getModel());
            artifactNode.setProperty(JCRConstants.SRAMP_ARTIFACT_TYPE, artifactType.getArtifactType().getType());
            //UserDefined
            if (UserDefinedArtifactType.class.isAssignableFrom(artifactType.getArtifactType().getTypeClass())) {
                // read the encoding from the header
                artifactNode.setProperty(JCRConstants.SRAMP_USER_TYPE, artifactType.getUserType());
            }
            //XMLDocument
            if (XmlDocument.class.isAssignableFrom(artifactType.getArtifactType().getTypeClass())) {
                // read the encoding from the header
                artifactNode.setProperty(JCRConstants.SRAMP_CONTENT_ENCODING, "UTF-8");
            }

            log.debug("Successfully saved {} to node={}",name, uuid);
            session.save();
            if (log.isDebugEnabled()) {
                printArtifactGraph(uuid, artifactType);
            }
            //now create the S-RAMP Artifact object from the JCR node
            BaseArtifactType baseTypeArtifact = JCRNodeToArtifactFactory.createArtifact(artifactNode, artifactType);
            return baseTypeArtifact;
        } catch (Throwable t) {
        	throw new RepositoryException(t);
        } finally {
        	IOUtils.closeQuietly(content);
            JCRRepository.logoutQuietly(session);
        }
    }

	/**
	 * @see org.overlord.sramp.repository.DerivedArtifacts#createDerivedArtifacts(org.overlord.sramp.ArtifactType, org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType)
	 */
	@Override
	public Collection<? extends DerivedArtifactType> createDerivedArtifacts(ArtifactType artifactType,
			BaseArtifactType artifact) throws DerivedArtifactsCreationException {
		// TODO use the nodes created by ModeShape sequencing to return the set of derived artifacts
		return Collections.<DerivedArtifactType>emptySet();
	}

    /**
     * @see org.overlord.sramp.repository.PersistenceManager#persistDerivedArtifact(org.s_ramp.xmlns._2010.s_ramp.DerivedArtifactType)
     */
    @Override
    public void persistDerivedArtifact(DerivedArtifactType artifact) {
    	// TODO update the derived artifact with any additional properties
    }

    /**
     * @see org.overlord.sramp.repository.PersistenceManager#getArtifact(java.lang.String, org.overlord.sramp.ArtifactType)
     */
    @Override
    public BaseArtifactType getArtifact(String uuid, ArtifactType type) throws RepositoryException {
        Session session = null;
        String artifactPath = MapToJCRPath.getArtifactPath(uuid, type);

        try {
            session = JCRRepository.getSession();
            if (session.nodeExists(artifactPath)) {
	            Node artifactNode = session.getNode(artifactPath);
	            // Create an artifact from the sequenced node
	            return JCRNodeToArtifactFactory.createArtifact(artifactNode, type);
            } else {
            	return null;
            }
        } catch (RepositoryException re) {
        	throw re;
        } catch (Throwable t) {
        	throw new RepositoryException(t);
        } finally {
            JCRRepository.logoutQuietly(session);
        }
    }

    /**
     * @see org.overlord.sramp.repository.PersistenceManager#getArtifactContent(java.lang.String, org.overlord.sramp.ArtifactType)
     */
    @Override
    public InputStream getArtifactContent(String uuid, ArtifactType type) throws RepositoryException {
        Session session = null;
        String artifactPath = MapToJCRPath.getArtifactPath(uuid, type);

        try {
            session = JCRRepository.getSession();
            Node artifactNode = session.getNode(artifactPath);
            Node artifactContentNode = artifactNode.getNode("jcr:content");
			File tempFile = saveToTempFile(artifactContentNode);
			return new DeleteOnCloseFileInputStream(tempFile);
        } catch (Throwable t) {
        	throw new RepositoryException(t);
		} finally {
            JCRRepository.logoutQuietly(session);
        }
    }

    /**
     * @see org.overlord.sramp.repository.PersistenceManager#updateArtifact(org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType, org.overlord.sramp.ArtifactType)
     */
    @Override
    public void updateArtifact(BaseArtifactType artifact, ArtifactType type) throws RepositoryException {
        Session session = null;
        String artifactPath = MapToJCRPath.getArtifactPath(artifact.getUuid(), type);

        try {
            session = JCRRepository.getSession();
            Node artifactNode = session.getNode(artifactPath);
            if (artifactNode == null) {
            	throw new RepositoryException("No artifact found with UUID: " + artifact.getUuid());
            }
            UpdateJCRNodeFromArtifactVisitor visitor = new UpdateJCRNodeFromArtifactVisitor(artifactNode);
            ArtifactVisitorHelper.visitArtifact(visitor, artifact);
            if (visitor.hasError())
            	throw visitor.getError();
            session.save();
        } catch (RepositoryException e) {
        	throw e;
        } catch (Throwable t) {
        	throw new RepositoryException(t);
        } finally {
            JCRRepository.logoutQuietly(session);
        }
    }

    /**
     * @see org.overlord.sramp.repository.PersistenceManager#updateArtifactContent(java.lang.String, org.overlord.sramp.ArtifactType, java.io.InputStream)
     */
    @Override
    public void updateArtifactContent(String uuid, ArtifactType artifactType, InputStream content) throws RepositoryException {
        Session session = null;
        String artifactPath = MapToJCRPath.getArtifactPath(uuid, artifactType);

        try {
            session = JCRRepository.getSession();
            Node artifactNode = session.getNode(artifactPath);
            if (artifactNode == null) {
            	throw new RepositoryException("No artifact found with UUID: " + uuid);
            }
            JcrTools tools = new JcrTools();
            tools.uploadFile(session, artifactPath, content);

            session.save();
        } catch (RepositoryException e) {
        	throw e;
        } catch (Throwable t) {
        	throw new RepositoryException(t);
        } finally {
            JCRRepository.logoutQuietly(session);
            IOUtils.closeQuietly(content);
        }
    }

    /**
     * @see org.overlord.sramp.repository.PersistenceManager#deleteArtifact(java.lang.String, org.overlord.sramp.ArtifactType)
     */
    @Override
    public void deleteArtifact(String uuid, ArtifactType artifactType) throws RepositoryException {
        Session session = null;
        String artifactPath = MapToJCRPath.getArtifactPath(uuid, artifactType);

        try {
            session = JCRRepository.getSession();
            if (session.nodeExists(artifactPath)) {
            	session.getNode(artifactPath).remove();
            } else {
            	throw new RepositoryException("Artifact not found.");
            }
            session.save();
        } catch (RepositoryException e) {
        	throw e;
        } catch (Throwable t) {
        	throw new RepositoryException(t);
        } finally {
        	JCRRepository.logoutQuietly(session);
        }
    }

    /**
     * @see org.overlord.sramp.repository.PersistenceManager#getArtifacts(org.overlord.sramp.ArtifactType)
     */
    @Override
    public List<BaseArtifactType> getArtifacts(ArtifactType type) throws RepositoryException {
    	List<BaseArtifactType> artifacts = new ArrayList<BaseArtifactType>();
        Session session = null;
        String artifactTypePath = MapToJCRPath.getArtifactTypePath(type);

        try {
            session = JCRRepository.getSession();
            Node artifactNode = session.getNode(artifactTypePath);
            List<Node> collectedNodes = new ArrayList<Node>();
            getNodes(artifactNode, collectedNodes);
            for (Node node : collectedNodes) {
                BaseArtifactType artifact = JCRNodeToArtifactFactory.createArtifact(node, type);
                artifacts.add(artifact);
			}
            return artifacts;
        } catch (RepositoryException re) {
        	throw re;
        } catch (Throwable t) {
        	throw new RepositoryException(t);
        } finally {
            JCRRepository.logoutQuietly(session);
        }
    }

    /**
     * Recursive method for traversing the tree of nodes looking for nodes of a
     * particular type.
	 * @param node the parent {@link Node} to navigate
	 * @return {@link List} of nodes matching the type
     * @throws Exception
	 */
	private void getNodes(Node node, List<Node> collectedNodes) throws Exception {
        NodeIterator nodeIter = node.getNodes();
        while (nodeIter.hasNext()) {
        	Node nextNode = nodeIter.nextNode();
        	if (isArtifactNode(nextNode))
        		collectedNodes.add(nextNode);
        	if (nextNode.hasNodes())
        		getNodes(nextNode, collectedNodes);
        }
	}

	/**
	 * Returns true if the given node is an S-RAMP artifact.
	 * @param node a JCR node
	 * @return boolean indicating if the node is an artifact
	 * @throws Exception
	 */
	private boolean isArtifactNode(Node node) throws Exception {
    	if (node.isNodeType(JCRConstants.SRAMP_BASE_ARTIFACT_TYPE)) {
    		return true;
		}
		return false;
	}

	/**
     * @see org.overlord.sramp.repository.PersistenceManager#printArtifactGraph(java.lang.String, org.overlord.sramp.ArtifactType)
     */
    @Override
    public void printArtifactGraph(String uuid, ArtifactType type) {
        Session session = null;
        String artifactPath = MapToJCRPath.getArtifactPath(uuid, type);
        try {
            session = JCRRepository.getSession();
            Node artifactNode = session.getNode(artifactPath);
            JcrTools tools = new JcrTools();
            tools.printSubgraph(artifactNode);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JCRRepository.logoutQuietly(session);
        }
    }

	/**
	 * Saves binary content from the given JCR content (jcr:content) node to a temporary
	 * file.
	 * @param jcrContentNode
	 * @param tempFile
	 * @throws Exception
	 */
	private File saveToTempFile(Node jcrContentNode) throws Exception {
		File file = File.createTempFile("sramp", ".jcr");
		Binary binary = null;
		InputStream content = null;
		OutputStream tempFileOS = null;

		try {
			binary = jcrContentNode.getProperty("jcr:data").getBinary();
			content = binary.getStream();
			tempFileOS = new FileOutputStream(file);
			IOUtils.copy(content, tempFileOS);
		} finally {
			IOUtils.closeQuietly(content);
			IOUtils.closeQuietly(tempFileOS);
			if (binary != null)
				binary.dispose();
		}

		return file;
	}

}
