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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.query.QueryResult;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.modeshape.jcr.JcrRepository.QueryLanguage;
import org.modeshape.jcr.api.JcrTools;
import org.overlord.sramp.ArtifactType;
import org.overlord.sramp.derived.ArtifactDeriver;
import org.overlord.sramp.derived.ArtifactDeriverFactory;
import org.overlord.sramp.ontology.SrampOntology;
import org.overlord.sramp.repository.DerivedArtifacts;
import org.overlord.sramp.repository.DerivedArtifactsCreationException;
import org.overlord.sramp.repository.DerivedArtifactsFactory;
import org.overlord.sramp.repository.PersistenceManager;
import org.overlord.sramp.repository.RepositoryException;
import org.overlord.sramp.repository.jcr.mapper.ArtifactToJCRNodeVisitor;
import org.overlord.sramp.repository.jcr.mapper.ArtifactToJCRNodeVisitor.JCRReferenceFactory;
import org.overlord.sramp.repository.jcr.mapper.JCRNodeToOntology;
import org.overlord.sramp.repository.jcr.mapper.OntologyToJCRNode;
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

    private static Logger log = LoggerFactory.getLogger(JCRPersistence.class);

	private static OntologyToJCRNode o2jcr = new OntologyToJCRNode();
	private static JCRNodeToOntology jcr2o = new JCRNodeToOntology();

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
    public BaseArtifactType persistArtifact(BaseArtifactType metaData, InputStream content) throws RepositoryException {
        Session session = null;
        try {
            session = JCRRepository.getSession();
            JcrTools tools = new JcrTools();
			if (metaData.getUuid() == null) {
				metaData.setUuid(UUID.randomUUID().toString());
			}
            String uuid = metaData.getUuid();
            ArtifactType artifactType = ArtifactType.valueOf(metaData);
            String name = metaData.getName();
            String artifactPath = MapToJCRPath.getArtifactPath(uuid, artifactType);
            log.debug("Uploading file {} to JCR.",name);

            Node artifactNode = tools.uploadFile(session, artifactPath, content);
            JCRUtils.setArtifactContentMimeType(artifactNode, artifactType.getMimeType());

            String jcrMixinName = artifactType.getArtifactType().getApiType().value();
            jcrMixinName = JCRConstants.SRAMP_ + StringUtils.uncapitalize(jcrMixinName);
            artifactNode.addMixin(jcrMixinName);
            // BaseArtifactType
            artifactNode.setProperty(JCRConstants.SRAMP_UUID, uuid);
            artifactNode.setProperty(JCRConstants.SRAMP_ARTIFACT_MODEL, artifactType.getArtifactType().getModel());
            artifactNode.setProperty(JCRConstants.SRAMP_ARTIFACT_TYPE, artifactType.getArtifactType().getType());
            // UserDefined
            if (UserDefinedArtifactType.class.isAssignableFrom(artifactType.getArtifactType().getTypeClass())) {
                // read the encoding from the header
                artifactNode.setProperty(JCRConstants.SRAMP_USER_TYPE, artifactType.getUserType());
            }
            // XMLDocument
            if (XmlDocument.class.isAssignableFrom(artifactType.getArtifactType().getTypeClass())) {
                // read the encoding from the header
                artifactNode.setProperty(JCRConstants.SRAMP_CONTENT_ENCODING, "UTF-8");
            }

            // Derive any content (this could modify the artifact currently being persisted *and*
            // create additional artifacts)
            InputStream cis = null;
			File tempFile = null;
            Collection<DerivedArtifactType> derivedArtifacts = null;
            try {
                Node artifactContentNode = artifactNode.getNode("jcr:content");
    			tempFile = saveToTempFile(artifactContentNode);
    			cis = FileUtils.openInputStream(tempFile);
                derivedArtifacts = DerivedArtifactsFactory.newInstance().deriveArtifacts(metaData, cis);
            } finally {
            	IOUtils.closeQuietly(cis);
            	FileUtils.deleteQuietly(tempFile);
            }

            // Update the JCR node with any properties included in the meta-data
            ArtifactToJCRNodeVisitor visitor = new ArtifactToJCRNodeVisitor(artifactNode, new JCRReferenceFactoryImpl(session));
            ArtifactVisitorHelper.visitArtifact(visitor, metaData);
            if (visitor.hasError())
            	throw visitor.getError();

            log.debug("Successfully saved {} to node={}", name, uuid);
            session.save();

            // Persist any derived artifacts.
            if (derivedArtifacts != null) {
            	persistDerivedArtifacts(session, artifactNode, derivedArtifacts);
            }

            // If debug is enabled, print the artifact graph
            if (log.isDebugEnabled()) {
            	printArtifactGraph(uuid, artifactType);
            }

            // Create the S-RAMP Artifact object from the JCR node
            BaseArtifactType artifact = JCRNodeToArtifactFactory.createArtifact(session, artifactNode, artifactType);

            return artifact;
        } catch (Throwable t) {
        	throw new RepositoryException(t);
        } finally {
        	IOUtils.closeQuietly(content);
            JCRRepository.logoutQuietly(session);
        }
    }

    /**
     * @see org.overlord.sramp.repository.DerivedArtifacts#deriveArtifacts(org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType, java.io.InputStream)
     */
    @Override
    public Collection<DerivedArtifactType> deriveArtifacts(BaseArtifactType sourceArtifact,
    		InputStream sourceArtifactContent) throws DerivedArtifactsCreationException {
    	try {
	    	ArtifactDeriver deriver = ArtifactDeriverFactory.createArtifactDeriver(ArtifactType.valueOf(sourceArtifact));
	    	return deriver.derive(sourceArtifact, sourceArtifactContent);
		} catch (IOException e) {
			throw new DerivedArtifactsCreationException(e);
    	}
    }

    /**
     * Persist any derived artifacts to JCR.
     * @param session
     * @param sourceArtifactNode
     * @param derivedArtifacts
     * @throws RepositoryException
     */
	protected void persistDerivedArtifacts(Session session, Node sourceArtifactNode, Collection<DerivedArtifactType> derivedArtifacts)
			throws RepositoryException {
        try {
            // Persist each of the derived nodes
            JCRReferenceFactoryImpl referenceFactory = new JCRReferenceFactoryImpl(session);
            Map<DerivedArtifactType, ArtifactToJCRNodeVisitor> deferredVisitors = new HashMap<DerivedArtifactType, ArtifactToJCRNodeVisitor>(derivedArtifacts.size());
            for (DerivedArtifactType derivedArtifact : derivedArtifacts) {
            	if (derivedArtifact.getUuid() == null) {
            		throw new RepositoryException("Missing UUID for derived artifact: " + derivedArtifact.getName());
            	}
                ArtifactType derivedArtifactType = ArtifactType.valueOf(derivedArtifact);
                String jcrNodeType = derivedArtifactType.getArtifactType().getApiType().value();
                jcrNodeType = JCRConstants.SRAMP_ + StringUtils.uncapitalize(jcrNodeType);

                // Create the JCR node and set some basic properties first.
                String nodeName = derivedArtifact.getUuid();
                Node derivedArtifactNode = sourceArtifactNode.addNode(nodeName, jcrNodeType);
                derivedArtifactNode.setProperty(JCRConstants.SRAMP_UUID, derivedArtifact.getUuid());
                derivedArtifactNode.setProperty(JCRConstants.SRAMP_ARTIFACT_MODEL, derivedArtifactType.getArtifactType().getModel());
                derivedArtifactNode.setProperty(JCRConstants.SRAMP_ARTIFACT_TYPE, derivedArtifactType.getArtifactType().getType());

                // Create the visitor that will be used later, once all the JCR nodes have
                // been created (to ensure that references can be resolved during the visit).
				ArtifactToJCRNodeVisitor visitor = new ArtifactToJCRNodeVisitor(derivedArtifactNode, referenceFactory);
				deferredVisitors.put(derivedArtifact, visitor);

                log.debug("Successfully saved derived artifact {} to node={}", derivedArtifact.getName(), derivedArtifact.getUuid());
            }

            // Save current changes so that references to nodes can be found.  Note that if
            // transactions are enabled, this will not actually persist to final storage.
            session.save();

            // Now run the Artifact->JCR Node visitor for each JCR node we created.  This will
            // cause the rest of the meta-data to be populated on the JCR node, including
            // properties and relationships (for example).  This is deferred so that references
            // created during processing of Relationships can be successful.
            for (Map.Entry<DerivedArtifactType, ArtifactToJCRNodeVisitor> entry : deferredVisitors.entrySet()) {
            	DerivedArtifactType derivedArtifact = entry.getKey();
            	ArtifactToJCRNodeVisitor visitor = entry.getValue();
	            ArtifactVisitorHelper.visitArtifact(visitor, derivedArtifact);
	            if (visitor.hasError())
	            	throw visitor.getError();
            }

            session.save();
        } catch (RepositoryException e) {
        	throw e;
        } catch (Throwable t) {
        	throw new RepositoryException(t);
        }
    }

    /**
     * @see org.overlord.sramp.repository.PersistenceManager#getArtifact(java.lang.String, org.overlord.sramp.ArtifactType)
     */
    @Override
    public BaseArtifactType getArtifact(String uuid, ArtifactType type) throws RepositoryException {
        Session session = null;
        try {
            session = JCRRepository.getSession();
			Node artifactNode = findArtifactNode(uuid, type, session);
            if (artifactNode != null) {
	            // Create an artifact from the sequenced node
	            return JCRNodeToArtifactFactory.createArtifact(session, artifactNode, type);
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
        try {
            session = JCRRepository.getSession();
            Node artifactNode = findArtifactNode(artifact.getUuid(), type, session);
            if (artifactNode == null) {
            	throw new RepositoryException("No artifact found with UUID: " + artifact.getUuid());
            }
            ArtifactToJCRNodeVisitor visitor = new ArtifactToJCRNodeVisitor(artifactNode, new JCRReferenceFactoryImpl(session));
            ArtifactVisitorHelper.visitArtifact(visitor, artifact);
            if (visitor.hasError())
            	throw visitor.getError();
            session.save();

            if (log.isDebugEnabled()) {
            	printArtifactGraph(artifact.getUuid(), type);
            }
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
	 * @see org.overlord.sramp.repository.PersistenceManager#persistOntology(org.overlord.sramp.ontology.SrampOntology)
	 */
	@Override
	public SrampOntology persistOntology(SrampOntology ontology) throws RepositoryException {
        Session session = null;
        if (ontology.getUuid() == null) {
        	ontology.setUuid(UUID.randomUUID().toString());
        }
        String ontologyPath = "/s-ramp/ontology/" + ontology.getUuid();

        try {
            session = JCRRepository.getSession();
            if (session.nodeExists(ontologyPath)) {
            	throw new RepositoryException("Ontology already exists.");
            } else {
            	JcrTools tools = new JcrTools();
            	Node ontologiesNode = tools.findOrCreateNode(session, "/s-ramp/ontology", null, null);
            	Node ontologyNode = ontologiesNode.addNode(ontology.getUuid(), "sramp:ontology");
            	o2jcr.write(ontology, ontologyNode);
                session.save();
                return ontology;
            }
        } catch (RepositoryException e) {
        	throw e;
        } catch (Throwable t) {
        	throw new RepositoryException(t);
        } finally {
        	JCRRepository.logoutQuietly(session);
        }
	}

	/**
	 * @see org.overlord.sramp.repository.PersistenceManager#getOntology(java.lang.String)
	 */
	@Override
	public SrampOntology getOntology(String uuid) throws RepositoryException {
        Session session = null;
        String ontologyPath = "/s-ramp/ontology/" + uuid;

        try {
        	SrampOntology ontology = null;
            session = JCRRepository.getSession();
            if (session.nodeExists(ontologyPath)) {
            	Node ontologyNode = session.getNode(ontologyPath);
            	ontology = new SrampOntology();
            	ontology.setUuid(uuid);
            	jcr2o.read(ontology, ontologyNode);
            } else {
            	throw new RepositoryException("Ontology does not exist.");
            }
            session.save();
        	return ontology;
        } catch (RepositoryException e) {
        	throw e;
        } catch (Throwable t) {
        	throw new RepositoryException(t);
        } finally {
        	JCRRepository.logoutQuietly(session);
        }
	}

	/**
	 * @see org.overlord.sramp.repository.PersistenceManager#getOntologies()
	 */
	@Override
	public List<SrampOntology> getOntologies() throws RepositoryException {
        Session session = null;

        try {
            session = JCRRepository.getSession();
        	JcrTools tools = new JcrTools();
        	Node ontologiesNode = tools.findOrCreateNode(session, "/s-ramp/ontology", null, null);
        	NodeIterator nodes = ontologiesNode.getNodes();
        	List<SrampOntology> ontologies = new ArrayList<SrampOntology>();
        	while (nodes.hasNext()) {
        		Node node = nodes.nextNode();
        		SrampOntology ontology = new SrampOntology();
        		jcr2o.read(ontology, node);
        		ontologies.add(ontology);
        	}
            return ontologies;
        } catch (Throwable t) {
        	throw new RepositoryException(t);
        } finally {
        	JCRRepository.logoutQuietly(session);
        }
	}

	/**
	 * @see org.overlord.sramp.repository.PersistenceManager#updateOntology(org.overlord.sramp.ontology.SrampOntology)
	 */
	@Override
	public void updateOntology(SrampOntology ontology) throws RepositoryException {
        Session session = null;
        String ontologyPath = "/s-ramp/ontology/" + ontology.getUuid();

        try {
            session = JCRRepository.getSession();
            if (session.nodeExists(ontologyPath)) {
            	Node ontologyNode = session.getNode(ontologyPath);
            	NodeIterator nodes = ontologyNode.getNodes();
            	while (nodes.hasNext()) {
            		Node child = nodes.nextNode();
            		child.remove();
            	}
            	o2jcr.write(ontology, ontologyNode);
            } else {
            	throw new RepositoryException("Ontology does not exist.");
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
	 * @see org.overlord.sramp.repository.PersistenceManager#deleteOntology(java.lang.String)
	 */
	@Override
	public void deleteOntology(String uuid) throws RepositoryException {
        Session session = null;
        String ontologyPath = "/s-ramp/ontology/" + uuid;

        try {
            session = JCRRepository.getSession();
            if (session.nodeExists(ontologyPath)) {
            	Node ontologyNode = session.getNode(ontologyPath);
            	ontologyNode.remove();
            } else {
            	throw new RepositoryException("Ontology does not exist.");
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
	 * Finds the JCR node for the given artifact (UUID + type).
	 * @param uuid
	 * @param type
	 * @param session
	 * @throws Exception
	 */
	private Node findArtifactNode(String uuid, ArtifactType type, Session session) throws Exception {
		Node artifactNode = null;
		if (type.getArtifactType().isDerived()) {
			artifactNode = findArtifactNodeByUuid(session, uuid);
		} else {
		    String artifactPath = MapToJCRPath.getArtifactPath(uuid, type);
		    if (session.nodeExists(artifactPath)) {
		        artifactNode = session.getNode(artifactPath);
		    }
		}
		return artifactNode;
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

	/**
	 * Utility method to find an s-ramp artifact node by its UUID.  Returns null if
	 * not found.  Throws an exception if too many JCR nodes are found with the given
	 * UUID.
	 * @param session
	 * @param artifactUuid
	 * @throws Exception
	 */
	private static Node findArtifactNodeByUuid(Session session, String artifactUuid) throws Exception {
        javax.jcr.query.QueryManager jcrQueryManager = session.getWorkspace().getQueryManager();
        String jcrSql2Query = String.format("SELECT * FROM [sramp:baseArtifactType] WHERE [sramp:uuid] = '%1$s'", artifactUuid);
        javax.jcr.query.Query jcrQuery = jcrQueryManager.createQuery(jcrSql2Query, QueryLanguage.JCR_SQL2);
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

	/**
	 * @see org.overlord.sramp.repository.PersistenceManager#shutdown()
	 */
	@Override
	public void shutdown() {
		JCRRepository.destroy();
	}

	/**
	 * An impl of a JCR reference factory.
	 */
	private static class JCRReferenceFactoryImpl implements JCRReferenceFactory {

		private Session session;

		/**
		 * Constructor.
		 * @param session
		 */
		public JCRReferenceFactoryImpl(Session session) {
			this.session = session;
		}

    	/**
    	 * @see org.overlord.sramp.repository.jcr.mapper.ArtifactToJCRNodeVisitor.JCRReferenceFactory#createReference(java.lang.String)
    	 */
    	@Override
    	public Value createReference(String uuid) throws Exception {
			Node node = findArtifactNodeByUuid(session, uuid);
			if (node != null) {
				return session.getValueFactory().createValue(node, false);
			} else {
				throw new Exception("Relationship creation error - failed to find an s-ramp artifact with target UUID: " + uuid);
			}
		}
	}

}
