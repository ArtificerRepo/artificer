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

import static org.overlord.sramp.repository.jcr.JCRConstants.OVERLORD;
import static org.overlord.sramp.repository.jcr.JCRConstants.OVERLORD_ARTIFACT;
import static org.overlord.sramp.repository.jcr.JCRConstants.OVERLORD_FILENAME;
import static org.overlord.sramp.repository.jcr.JCRConstants.SRAMP_UUID;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

import javax.jcr.LoginException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;

import org.apache.commons.io.IOUtils;
import org.modeshape.jcr.api.JcrTools;
import org.modeshape.jcr.api.nodetype.NodeTypeManager;
import org.overlord.sramp.ArtifactType;
import org.overlord.sramp.ArtifactVisitorHelper;
import org.overlord.sramp.repository.DerivedArtifacts;
import org.overlord.sramp.repository.DerivedArtifactsCreationException;
import org.overlord.sramp.repository.PersistenceManager;
import org.overlord.sramp.repository.UnsupportedFiletypeException;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.DerivedArtifactType;
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
        Session session = null;
        InputStream is = null;
        try {
            session = JCRRepository.getSession();
            NodeTypeManager manager = (NodeTypeManager) session.getWorkspace().getNodeTypeManager();
            session.setNamespacePrefix(OVERLORD, "http://www.jboss.org/overlord/1.0");
            
            if (! manager.hasNodeType(SRAMP_UUID)) {
                // Register the ModeShape S-RAMP node types ...
                is = this.getClass().getResourceAsStream("/org/modeshape/sequencer/sramp/sramp.cnd");
                manager.registerNodeTypes(is,true);
            }
            if (! manager.hasNodeType(OVERLORD_ARTIFACT)) {
                // Register the Overlord node types ...
                is = this.getClass().getResourceAsStream("/org/overlord/s-ramp/overlord.cnd");
                manager.registerNodeTypes(is,true);
            }
        } catch (LoginException e) {
            throw e;
        } catch (NoSuchWorkspaceException e) {
            throw e;
        } catch (RepositoryException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } finally {
        	IOUtils.closeQuietly(is);
            if ( session != null ) session.logout();
        }
    }
    
    /**
     * @see org.overlord.sramp.repository.PersistenceManager#persistArtifact(java.lang.String, org.overlord.sramp.ArtifactType, java.io.InputStream)
     */
    @Override
	public BaseArtifactType persistArtifact(String name, ArtifactType type, InputStream content)
			throws UnsupportedFiletypeException {
        Session session = null;
        String identifier = null;
        try {
            session = JCRRepository.getSession();
            JcrTools tools = new JcrTools();
            String uuid = UUID.randomUUID().toString();
            String artifactPath = MapToJCRPath.getArtifactPath(uuid, type);
            log.debug("Uploading file {} to JCR.",name);
            Node artifactNode = tools.uploadFile(session, artifactPath, content);
            identifier = artifactNode.getIdentifier();
            artifactNode.addMixin(OVERLORD_ARTIFACT);
            
            artifactNode.setProperty(SRAMP_UUID, uuid);
            artifactNode.setProperty(OVERLORD_FILENAME, name);
            log.debug("Successfully saved {} to node={}",name, uuid);
            String sequencedArtifactPath = MapToJCRPath.getSequencedArtifactPath(artifactPath);
			JCRRepository.getListener().addWaitingLatch(sequencedArtifactPath);
            session.save();
        } catch (LoginException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchWorkspaceException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (RepositoryException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
        	IOUtils.closeQuietly(content);
            session.logout();
        }
        
        return createArtifactInternal(identifier, type);
    }

    /**
     * Creates an artifact given a JCR identifier of a persisted artifact.
	 * @param identifier the unique JCR identifier
	 * @param type the artifact type
	 * @return an instance of a {@link BaseArtifactType}
	 */
	protected BaseArtifactType createArtifactInternal(String identifier, ArtifactType type) {
		Session session = null;
        try {
            session = JCRRepository.getSession();
            
            // Get the artifact node
            Node artifactNode = session.getNodeByIdentifier(identifier);
            String sequencedArtifactPath = MapToJCRPath.getSequencedArtifactPath(artifactNode.getPath());
            // Wait for sequencing
            JCRRepository.getListener().waitForLatch(sequencedArtifactPath);
            // Get the sequenced node
            Node sequencedNode = session.getNode(sequencedArtifactPath);

            // Update the sequenced node with some additional meta data
            sequencedNode.addMixin(OVERLORD_ARTIFACT);
            String uuid = artifactNode.getProperty(SRAMP_UUID).getValue().getString();
            String filename = artifactNode.getProperty(OVERLORD_FILENAME).getValue().getString();
            sequencedNode.setProperty(SRAMP_UUID, uuid);
            sequencedNode.setProperty(OVERLORD_FILENAME, filename);
            session.save();

            // Create an artifact from the sequenced node
            return JCRNodeToArtifactFactory.createArtifact(sequencedNode, type);
        } catch (UnsupportedRepositoryOperationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (LoginException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchWorkspaceException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (RepositoryException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedFiletypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
            session.logout();
        }
        
        return null;
	}

	/**
	 * @see org.overlord.sramp.repository.DerivedArtifacts#createDerivedArtifacts(org.overlord.sramp.ArtifactType, org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType)
	 */
	@Override
	public Collection<? extends DerivedArtifactType> createDerivedArtifacts(
			ArtifactType artifactType, BaseArtifactType artifact)
			throws DerivedArtifactsCreationException,
			UnsupportedFiletypeException {
		// TODO use the nodes created by ModeShape sequencing to return the set of derived artifacts
		return Collections.<DerivedArtifactType>emptySet();
	}
    
    /**
     * @see org.overlord.sramp.repository.PersistenceManager#persistDerivedArtifact(org.s_ramp.xmlns._2010.s_ramp.DerivedArtifactType)
     */
    @Override
    public String persistDerivedArtifact(DerivedArtifactType artifact) {
    	// TODO Auto-generated method stub
    	return null;
    }
    
    /**
     * @see org.overlord.sramp.repository.PersistenceManager#getArtifact(java.lang.String, org.overlord.sramp.ArtifactType)
     */
    @Override
    public BaseArtifactType getArtifact(String uuid, ArtifactType type) {
        Session session = null;
        String artifactPath = MapToJCRPath.getArtifactPath(uuid, type);
        String sequencedArtifactPath = MapToJCRPath.getSequencedArtifactPath(artifactPath);
        
        try {
            session = JCRRepository.getSession();
            Node sequencedNode = session.getNode(sequencedArtifactPath);
            // Create an artifact from the sequenced node
            return JCRNodeToArtifactFactory.createArtifact(sequencedNode, type);
        } catch (LoginException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchWorkspaceException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (RepositoryException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            session.logout();
        }
        return null;
    }
    
    /**
     * @see org.overlord.sramp.repository.PersistenceManager#updateArtifact(org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType, org.overlord.sramp.ArtifactType)
     */
    @Override
    public void updateArtifact(BaseArtifactType artifact, ArtifactType type) {
        Session session = null;
        String artifactPath = MapToJCRPath.getArtifactPath(artifact.getUuid(), type);
        String sequencedArtifactPath = MapToJCRPath.getSequencedArtifactPath(artifactPath);
        
        try {
            session = JCRRepository.getSession();
            Node sequencedNode = session.getNode(sequencedArtifactPath);
            UpdateJCRNodeFromArtifactVisitor visitor = new UpdateJCRNodeFromArtifactVisitor(sequencedNode);
            ArtifactVisitorHelper.visitArtifact(visitor, artifact);
            session.save();
        } catch (LoginException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchWorkspaceException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (RepositoryException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            session.logout();
        }
    }
    
    /**
     * @see org.overlord.sramp.repository.PersistenceManager#printArtifactGraph(java.lang.String, org.overlord.sramp.ArtifactType)
     */
    @Override
    public void printArtifactGraph(String uuid, ArtifactType type) {
        Session session = null;
        String artifactPath = MapToJCRPath.getArtifactPath(uuid, type);
        String sequencedArtifactPath = MapToJCRPath.getSequencedArtifactPath(artifactPath);
        
        try {
            session = JCRRepository.getSession();
            Node sequencedNode = session.getNode(sequencedArtifactPath);
            JcrTools tools = new JcrTools();
            tools.printSubgraph(sequencedNode);
        } catch (LoginException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchWorkspaceException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (RepositoryException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            session.logout();
        }
    }

}
