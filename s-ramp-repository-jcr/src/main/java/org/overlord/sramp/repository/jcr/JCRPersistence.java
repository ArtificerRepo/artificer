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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.Value;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DocumentArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XmlDocument;
import org.overlord.sramp.common.ArtifactNotFoundException;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.common.SrampException;
import org.overlord.sramp.common.SrampModelUtils;
import org.overlord.sramp.common.SrampServerException;
import org.overlord.sramp.common.derived.ArtifactDeriver;
import org.overlord.sramp.common.derived.ArtifactDeriverFactory;
import org.overlord.sramp.common.derived.LinkerContext;
import org.overlord.sramp.common.ontology.InvalidClassifiedByException;
import org.overlord.sramp.common.ontology.OntologyAlreadyExistsException;
import org.overlord.sramp.common.ontology.OntologyNotFoundException;
import org.overlord.sramp.common.ontology.SrampOntology;
import org.overlord.sramp.common.ontology.SrampOntology.Class;
import org.overlord.sramp.common.visitors.ArtifactVisitorHelper;
import org.overlord.sramp.repository.DerivedArtifacts;
import org.overlord.sramp.repository.DerivedArtifactsFactory;
import org.overlord.sramp.repository.PersistenceManager;
import org.overlord.sramp.repository.jcr.audit.JCRAuditConstants;
import org.overlord.sramp.repository.jcr.mapper.ArtifactToJCRNodeVisitor;
import org.overlord.sramp.repository.jcr.mapper.ArtifactToJCRNodeVisitor.JCRReferenceFactory;
import org.overlord.sramp.repository.jcr.mapper.JCRNodeToOntology;
import org.overlord.sramp.repository.jcr.mapper.OntologyToJCRNode;
import org.overlord.sramp.repository.jcr.util.DeleteOnCloseFileInputStream;
import org.overlord.sramp.repository.jcr.util.JCRUtils;
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
public class JCRPersistence extends AbstractJCRManager implements PersistenceManager, DerivedArtifacts, ClassificationHelper {

	private static Logger log = LoggerFactory.getLogger(JCRPersistence.class);

	private static OntologyToJCRNode o2jcr = new OntologyToJCRNode();
	private static JCRNodeToOntology jcr2o = new JCRNodeToOntology();

	//	private Map<String, SrampOntology> ontologyCache = new HashMap<String, SrampOntology>();

	/**
	 * Default constructor.
	 */
	public JCRPersistence() {
	}

	/**
	 * @see org.overlord.sramp.common.repository.PersistenceManager#persistArtifact(java.lang.String, org.overlord.sramp.common.ArtifactType, java.io.InputStream)
	 */
	@Override
	public BaseArtifactType persistArtifact(BaseArtifactType metaData, InputStream content) throws SrampException {
		Session session = null;
		try {
			session = JCRRepositoryFactory.getSession();
			JCRUtils tools = new JCRUtils();
			if (metaData.getUuid() == null) {
				metaData.setUuid(UUID.randomUUID().toString());
			}
			String uuid = metaData.getUuid();
			ArtifactType artifactType = ArtifactType.valueOf(metaData);
			String name = metaData.getName();
			String artifactPath = MapToJCRPath.getArtifactPath(uuid, artifactType);
			log.debug("Uploading file {} to JCR.",name);

			Node artifactNode = null;
			boolean isDocumentArtifact = SrampModelUtils.isDocumentArtifact(metaData);
            if (content == null && !isDocumentArtifact) {
			    artifactNode = tools.findOrCreateNode(session, artifactPath, "nt:folder", JCRConstants.SRAMP_NON_DOCUMENT_TYPE);
			} else {
			    artifactNode = tools.uploadFile(session, artifactPath, content);
	            JCRUtils.setArtifactContentMimeType(artifactNode, artifactType.getMimeType());
			}

			String jcrMixinName = artifactType.getArtifactType().getApiType().value();
			jcrMixinName = JCRConstants.SRAMP_ + StringUtils.uncapitalize(jcrMixinName);
			artifactNode.addMixin(jcrMixinName);
			// BaseArtifactType
			artifactNode.setProperty(JCRConstants.SRAMP_UUID, uuid);
			artifactNode.setProperty(JCRConstants.SRAMP_ARTIFACT_MODEL, artifactType.getArtifactType().getModel());
			artifactNode.setProperty(JCRConstants.SRAMP_ARTIFACT_TYPE, artifactType.getArtifactType().getType());
			// Extended
			if (ExtendedArtifactType.class.isAssignableFrom(artifactType.getArtifactType().getTypeClass())) {
				// read the encoding from the header
				artifactNode.setProperty(JCRConstants.SRAMP_EXTENDED_TYPE, artifactType.getExtendedType());
			}
			// Extended Document
            if (ExtendedDocument.class.isAssignableFrom(artifactType.getArtifactType().getTypeClass())) {
                // read the encoding from the header
                artifactNode.setProperty(JCRConstants.SRAMP_EXTENDED_TYPE, artifactType.getExtendedType());
            }
			// Document
			if (DocumentArtifactType.class.isAssignableFrom(artifactType.getArtifactType().getTypeClass())) {
				artifactNode.setProperty(JCRConstants.SRAMP_CONTENT_TYPE, artifactType.getMimeType());
				artifactNode.setProperty(JCRConstants.SRAMP_CONTENT_SIZE, artifactNode.getProperty("jcr:content/jcr:data").getLength());
				// TODO add content hash here - SHA1
			}
			// XMLDocument
			if (XmlDocument.class.isAssignableFrom(artifactType.getArtifactType().getTypeClass())) {
				// read the encoding from the header
				artifactNode.setProperty(JCRConstants.SRAMP_CONTENT_ENCODING, "UTF-8");
			}

			// Update the JCR node with any properties included in the meta-data
			ArtifactToJCRNodeVisitor visitor = new ArtifactToJCRNodeVisitor(artifactType, artifactNode,
					new JCRReferenceFactoryImpl(session), this);
			ArtifactVisitorHelper.visitArtifact(visitor, metaData);
			if (visitor.hasError())
				throw visitor.getError();

			log.debug("Successfully saved {} to node={}", name, uuid);
            session.getWorkspace().getObservationManager().setUserData(JCRAuditConstants.AUDIT_BUNDLE_ARTIFACT_ADDED_PHASE1);
			session.save();

            // Derive any content (this could modify the artifact currently being persisted *and*
            // create additional artifacts).  So when we're done, we need to save any potential
			// changes to the original artifact as well as persist any derived artifacts.  Only
			// do this for document style artifacts.
            Collection<BaseArtifactType> derivedArtifacts = null;
			if (isDocumentArtifact) {
                InputStream cis = null;
                File tempFile = null;
                try {
                    Node artifactContentNode = artifactNode.getNode("jcr:content");
                    tempFile = saveToTempFile(artifactContentNode);
                    cis = FileUtils.openInputStream(tempFile);
                    derivedArtifacts = DerivedArtifactsFactory.newInstance().deriveArtifacts(metaData, cis);
                } finally {
                    IOUtils.closeQuietly(cis);
                    FileUtils.deleteQuietly(tempFile);
                }

    			// Persist any derived artifacts.
    			if (derivedArtifacts != null) {
    				persistDerivedArtifacts(session, artifactNode, derivedArtifacts);
    			}

                // Update the JCR node again, this time with any properties/relationships added to the meta-data
                // by the deriver
                visitor = new ArtifactToJCRNodeVisitor(artifactType, artifactNode,
                        new JCRReferenceFactoryImpl(session), this);
                ArtifactVisitorHelper.visitArtifact(visitor, metaData);
                if (visitor.hasError())
                    throw visitor.getError();

                // JCR persist point - phase 2 of artifact create
                session.getWorkspace().getObservationManager().setUserData(JCRAuditConstants.AUDIT_BUNDLE_ARTIFACT_ADDED_PHASE2);
                session.save();

                // Now execute the derived artifact linker phase, creating relationships between the various
                // artifacts derived above.
                if (derivedArtifacts != null && !derivedArtifacts.isEmpty()) {
                    LinkerContext context = new JCRLinkerContext(session);
                    DerivedArtifactsFactory.newInstance().linkArtifacts(context, metaData, derivedArtifacts);
                    persistDerivedArtifactsRelationships(session, artifactNode, derivedArtifacts);
                }

                // JCR persist point - phase 3 of artifact create (only for document style artifacts
                // with derived content)
                session.getWorkspace().getObservationManager().setUserData(JCRAuditConstants.AUDIT_BUNDLE_ARTIFACT_ADDED_PHASE3);
                session.save();
			}

			// If debug is enabled, print the artifact graph
			if (log.isDebugEnabled()) {
				printArtifactGraph(uuid, artifactType);
			}

			// Create the S-RAMP Artifact object from the JCR node
			BaseArtifactType artifact = JCRNodeToArtifactFactory.createArtifact(session, artifactNode, artifactType);

			return artifact;
        } catch (SrampException se) {
            throw se;
        } catch (Throwable t) {
            throw new SrampServerException(t);
		} finally {
			IOUtils.closeQuietly(content);
			JCRRepositoryFactory.logoutQuietly(session);
		}
	}

    /**
	 * @see org.overlord.sramp.repository.DerivedArtifacts#deriveArtifacts(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType, java.io.InputStream)
	 */
	@Override
	public Collection<BaseArtifactType> deriveArtifacts(BaseArtifactType sourceArtifact,
			InputStream sourceArtifactContent) throws SrampException {
		try {
			ArtifactDeriver deriver = ArtifactDeriverFactory.createArtifactDeriver(ArtifactType.valueOf(sourceArtifact));
			Collection<BaseArtifactType> derivedArtifacts = deriver.derive(sourceArtifact, sourceArtifactContent);
			log.debug("Successfully derived {} artifacts from {}.", derivedArtifacts.size(), sourceArtifact.getUuid());
			return derivedArtifacts;
		} catch (IOException e) {
			throw new SrampServerException(e);
		}
	}

	/**
	 * @see org.overlord.sramp.repository.DerivedArtifacts#linkArtifacts(org.overlord.sramp.common.derived.LinkerContext, org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType, java.util.Collection)
	 */
	@Override
	public void linkArtifacts(LinkerContext context, BaseArtifactType sourceArtifact,
	        Collection<BaseArtifactType> derivedArtifacts) throws SrampException {
        try {
            ArtifactDeriver deriver = ArtifactDeriverFactory.createArtifactDeriver(ArtifactType.valueOf(sourceArtifact));
            deriver.link(context, sourceArtifact, derivedArtifacts);
            log.debug("Successfully linked {} artifacts from {}.", derivedArtifacts.size(), sourceArtifact.getUuid());
        } catch (Exception e) {
            throw new SrampServerException(e);
        }
	}

	/**
	 * Persist any derived artifacts to JCR.
	 * @param session
	 * @param sourceArtifactNode
	 * @param derivedArtifacts
	 * @throws SrampException
	 */
    protected void persistDerivedArtifacts(Session session, Node sourceArtifactNode, Collection<BaseArtifactType> derivedArtifacts)
			throws SrampException {
		try {
			// Persist each of the derived nodes
			for (BaseArtifactType derivedArtifact : derivedArtifacts) {
				if (derivedArtifact.getUuid() == null) {
					throw new SrampServerException("Missing UUID for derived artifact: " + derivedArtifact.getName());
				}
				ArtifactType derivedArtifactType = ArtifactType.valueOf(derivedArtifact);
				String jcrNodeType = derivedArtifactType.getArtifactType().getApiType().value();
				if (derivedArtifactType.isExtendedType()) {
				    jcrNodeType = "extendedDerivedArtifactType";
				    derivedArtifactType.setExtendedDerivedType(true);
				}
				jcrNodeType = JCRConstants.SRAMP_ + StringUtils.uncapitalize(jcrNodeType);

				// Create the JCR node and set some basic properties first.
				String nodeName = derivedArtifact.getUuid();
				Node derivedArtifactNode = sourceArtifactNode.addNode(nodeName, jcrNodeType);
				derivedArtifactNode.setProperty(JCRConstants.SRAMP_UUID, derivedArtifact.getUuid());
				derivedArtifactNode.setProperty(JCRConstants.SRAMP_ARTIFACT_MODEL, derivedArtifactType.getArtifactType().getModel());
				derivedArtifactNode.setProperty(JCRConstants.SRAMP_ARTIFACT_TYPE, derivedArtifactType.getArtifactType().getType());
	            // Extended
	            if (ExtendedArtifactType.class.isAssignableFrom(derivedArtifactType.getArtifactType().getTypeClass())) {
	                // read the encoding from the header
	                derivedArtifactNode.setProperty(JCRConstants.SRAMP_EXTENDED_TYPE, derivedArtifactType.getExtendedType());
	            }

	            // It's definitely derived.
	            derivedArtifactNode.setProperty("sramp:derived", true);

				// Create the visitor that will be used to write the artifact information to the JCR node
                ArtifactToJCRNodeVisitor visitor = new ArtifactToJCRNodeVisitor(derivedArtifactType,
                        derivedArtifactNode, null, this);
                visitor.setProcessRelationships(false);
                ArtifactVisitorHelper.visitArtifact(visitor, derivedArtifact);
                if (visitor.hasError())
                    throw visitor.getError();

				log.debug("Successfully saved derived artifact {} to node={}", derivedArtifact.getName(), derivedArtifact.getUuid());
			}

			// Save current changes so that references to nodes can be found.  Note that if
			// transactions are enabled, this will not actually persist to final storage.
			session.getWorkspace().getObservationManager().setUserData(JCRAuditConstants.AUDIT_BUNDLE_DERIVED_ARTIFACTS_ADDED_PHASE1);
			session.save();

			log.debug("Successfully saved {} artifacts.", derivedArtifacts.size());
		} catch (SrampException e) {
			throw e;
		} catch (Throwable t) {
			throw new SrampServerException(t);
		}
	}

    /**
     * Perists the derived artifacts again due to
     * @param session
     * @param artifactNode
     * @param derivedArtifacts
     */
    private void persistDerivedArtifactsRelationships(Session session, Node sourceArtifactNode,
            Collection<BaseArtifactType> derivedArtifacts) throws SrampException {
        try {
            // Persist each of the derived nodes
            JCRReferenceFactoryImpl referenceFactory = new JCRReferenceFactoryImpl(session);
            for (BaseArtifactType derivedArtifact : derivedArtifacts) {
                ArtifactType derivedArtifactType = ArtifactType.valueOf(derivedArtifact);
                if (derivedArtifactType.isExtendedType()) {
                    derivedArtifactType.setExtendedDerivedType(true);
                }
                Node derivedArtifactNode = sourceArtifactNode.getNode(derivedArtifact.getUuid());

                // Create the visitor that will be used to write the artifact information to the JCR node
                ArtifactToJCRNodeVisitor visitor = new ArtifactToJCRNodeVisitor(derivedArtifactType,
                        derivedArtifactNode, referenceFactory, this);
                visitor.setProcessRelationships(true);
                ArtifactVisitorHelper.visitArtifact(visitor, derivedArtifact);
                if (visitor.hasError())
                    throw visitor.getError();

                log.debug("Successfully saved derived artifact {}'s relationships.", derivedArtifact.getName());
            }

            // Perist phase 2 (the relationships)
            session.getWorkspace().getObservationManager().setUserData(JCRAuditConstants.AUDIT_BUNDLE_DERIVED_ARTIFACTS_ADDED_PHASE2);
            session.save();

            log.debug("Successfully saved {} artifacts (phase 2).", derivedArtifacts.size());
        } catch (SrampException e) {
            throw e;
        } catch (Throwable t) {
            throw new SrampServerException(t);
        }

    }

	/**
	 * @see org.overlord.sramp.common.repository.PersistenceManager#getArtifact(java.lang.String, org.overlord.sramp.common.ArtifactType)
	 */
	@Override
	public BaseArtifactType getArtifact(String uuid, ArtifactType type) throws SrampException {
		Session session = null;
		try {
			session = JCRRepositoryFactory.getSession();
			Node artifactNode = findArtifactNode(uuid, type, session);
			if (artifactNode != null) {
			    // In the case of an extended type, we might be wrong about which one...
			    if (type.isExtendedType()) {
                    String t = artifactNode.getProperty(JCRConstants.SRAMP_ARTIFACT_TYPE).getString();
                    if (ExtendedDocument.class.getSimpleName().equals(t)) {
                        String e = type.getExtendedType();
                        type = ArtifactType.valueOf(BaseArtifactEnum.EXTENDED_DOCUMENT);
                        type.setExtendedType(e);
                    }
			    }
                // Create an artifact from the sequenced node
				return JCRNodeToArtifactFactory.createArtifact(session, artifactNode, type);
			} else {
				return null;
			}
		} catch (SrampException se) {
		    throw se;
		} catch (Throwable t) {
			throw new SrampServerException(t);
		} finally {
			JCRRepositoryFactory.logoutQuietly(session);
		}
	}

	/**
	 * @see org.overlord.sramp.common.repository.PersistenceManager#getArtifactContent(java.lang.String, org.overlord.sramp.common.ArtifactType)
	 */
	@Override
	public InputStream getArtifactContent(String uuid, ArtifactType type) throws SrampException {
		Session session = null;

		try {
			session = JCRRepositoryFactory.getSession();

			Node artifactNode = findArtifactNode(uuid, type, session);
			if (artifactNode == null) {
			    throw new ArtifactNotFoundException(uuid);
			}
		    // In the case of an extended type, we might be wrong about which one...
		    if (type.isExtendedType()) {
		        String t = artifactNode.getProperty(JCRConstants.SRAMP_ARTIFACT_TYPE).getString();
		        if (ExtendedDocument.class.getSimpleName().equals(t)) {
		            String e = type.getExtendedType();
		            type = ArtifactType.valueOf(BaseArtifactEnum.EXTENDED_DOCUMENT);
		            type.setExtendedType(e);
		        }
		    }
			Node artifactContentNode = artifactNode.getNode("jcr:content");
			File tempFile = saveToTempFile(artifactContentNode);
			return new DeleteOnCloseFileInputStream(tempFile);
        } catch (SrampException se) {
            throw se;
        } catch (Throwable t) {
            throw new SrampServerException(t);
		} finally {
			JCRRepositoryFactory.logoutQuietly(session);
		}
	}

	/**
	 * @see org.overlord.sramp.common.repository.PersistenceManager#updateArtifact(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType, org.overlord.sramp.common.ArtifactType)
	 */
	@Override
	public void updateArtifact(BaseArtifactType artifact, ArtifactType type) throws SrampException {
		Session session = null;
		try {
			session = JCRRepositoryFactory.getSession();
			Node artifactNode = findArtifactNode(artifact.getUuid(), type, session);
			if (artifactNode == null) {
				throw new ArtifactNotFoundException(artifact.getUuid());
			}
			ArtifactToJCRNodeVisitor visitor = new ArtifactToJCRNodeVisitor(type, artifactNode,
					new JCRReferenceFactoryImpl(session), this);
			ArtifactVisitorHelper.visitArtifact(visitor, artifact);
			if (visitor.hasError())
				throw visitor.getError();
            session.getWorkspace().getObservationManager().setUserData(JCRAuditConstants.AUDIT_BUNDLE_ARTIFACT_UPDATED);
			session.save();

			log.debug("Successfully updated meta-data for artifact {}.", artifact.getUuid());

			if (log.isDebugEnabled()) {
				printArtifactGraph(artifact.getUuid(), type);
			}
        } catch (SrampException se) {
            throw se;
        } catch (Throwable t) {
            throw new SrampServerException(t);
		} finally {
			JCRRepositoryFactory.logoutQuietly(session);
		}
	}

	/**
	 * @see org.overlord.sramp.common.repository.PersistenceManager#updateArtifactContent(java.lang.String, org.overlord.sramp.common.ArtifactType, java.io.InputStream)
	 */
	@Override
	public void updateArtifactContent(String uuid, ArtifactType artifactType, InputStream content) throws SrampException {
		Session session = null;
		try {
			session = JCRRepositoryFactory.getSession();
            Node artifactNode = findArtifactNode(uuid, artifactType, session);
            if (artifactNode == null) {
                throw new ArtifactNotFoundException(uuid);
            }
			JCRUtils tools = new JCRUtils();
			tools.uploadFile(session, artifactNode.getPath(), content);
			JCRUtils.setArtifactContentMimeType(artifactNode, artifactType.getMimeType());

			// Document
			if (DocumentArtifactType.class.isAssignableFrom(artifactType.getArtifactType().getTypeClass())) {
				artifactNode.setProperty(JCRConstants.SRAMP_CONTENT_TYPE, artifactType.getMimeType());
				artifactNode.setProperty(JCRConstants.SRAMP_CONTENT_SIZE, artifactNode.getProperty("jcr:content/jcr:data").getLength());
				// TODO also handle the hash here
			}

			// TODO delete and re-create the derived artifacts?  what if some of them have properties or classifications?

            session.getWorkspace().getObservationManager().setUserData(JCRAuditConstants.AUDIT_BUNDLE_ARTIFACT_CONTENT_UPDATED);
			session.save();
			log.debug("Successfully updated content for artifact {}.", uuid);
        } catch (SrampException se) {
            throw se;
        } catch (Throwable t) {
            throw new SrampServerException(t);
		} finally {
			JCRRepositoryFactory.logoutQuietly(session);
			IOUtils.closeQuietly(content);
		}
	}

	/**
	 * @see org.overlord.sramp.common.repository.PersistenceManager#deleteArtifact(java.lang.String, org.overlord.sramp.common.ArtifactType)
	 */
	@Override
	public void deleteArtifact(String uuid, ArtifactType artifactType) throws SrampException {
		Session session = null;
		try {
			session = JCRRepositoryFactory.getSession();
            Node artifactNode = findArtifactNode(uuid, artifactType, session);
            if (artifactNode == null) {
                throw new ArtifactNotFoundException(uuid);
            }
            artifactNode.remove();
            session.getWorkspace().getObservationManager().setUserData(JCRAuditConstants.AUDIT_BUNDLE_ARTIFACT_DELETED);
			session.save();
			log.debug("Successfully deleted artifact {}.", uuid);
        } catch (SrampException se) {
            throw se;
        } catch (Throwable t) {
            throw new SrampServerException(t);
		} finally {
			JCRRepositoryFactory.logoutQuietly(session);
		}
	}

	/**
	 * @see org.overlord.sramp.common.repository.PersistenceManager#persistOntology(org.overlord.sramp.common.ontology.SrampOntology)
	 */
	@Override
	public SrampOntology persistOntology(SrampOntology ontology) throws SrampException {
		Session session = null;
		if (ontology.getUuid() == null) {
			ontology.setUuid(UUID.randomUUID().toString());
		}
		String ontologyPath = "/s-ramp/ontology/" + ontology.getUuid();

		try {
			session = JCRRepositoryFactory.getSession();
			if (session.nodeExists(ontologyPath)) {
			    throw new OntologyAlreadyExistsException(ontology.getUuid());
			} else {
			    JCRUtils tools = new JCRUtils();
				Node ontologiesNode = tools.findOrCreateNode(session, "/s-ramp/ontology", "nt:folder");
				Node ontologyNode = ontologiesNode.addNode(ontology.getUuid(), "sramp:ontology");
				o2jcr.write(ontology, ontologyNode);
	            session.getWorkspace().getObservationManager().setUserData(JCRAuditConstants.AUDIT_BUNDLE_ONTOLOGY_ADDED);
				session.save();
				log.debug("Successfully saved ontology {}.", ontology.getUuid());
				return ontology;
			}
        } catch (SrampException se) {
            throw se;
        } catch (Throwable t) {
            throw new SrampServerException(t);
		} finally {
			JCRRepositoryFactory.logoutQuietly(session);
		}
	}

	/**
	 * @see org.overlord.sramp.common.repository.PersistenceManager#getOntology(java.lang.String)
	 */
	@Override
	public SrampOntology getOntology(String uuid) throws SrampException {
		Session session = null;
		String ontologyPath = "/s-ramp/ontology/" + uuid;

		try {
			SrampOntology ontology = null;
			session = JCRRepositoryFactory.getSession();
			if (session.nodeExists(ontologyPath)) {
				Node ontologyNode = session.getNode(ontologyPath);
				ontology = new SrampOntology();
				ontology.setUuid(uuid);
				jcr2o.read(ontology, ontologyNode);
			} else {
			    throw new OntologyNotFoundException(uuid);
			}
			return ontology;
        } catch (SrampException se) {
            throw se;
        } catch (Throwable t) {
            throw new SrampServerException(t);
		} finally {
			JCRRepositoryFactory.logoutQuietly(session);
		}
	}

	/**
	 * @see org.overlord.sramp.common.repository.PersistenceManager#getOntologies()
	 */
	@Override
	public List<SrampOntology> getOntologies() throws SrampException {
		// TODO add caching based on the last modified date of the ontology node
		Session session = null;

		try {
			session = JCRRepositoryFactory.getSession();
			JCRUtils tools = new JCRUtils();
			Node ontologiesNode = tools.findOrCreateNode(session, "/s-ramp/ontology", "nt:folder");
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
            throw new SrampServerException(t);
		} finally {
			JCRRepositoryFactory.logoutQuietly(session);
		}
	}

	/**
	 * @see org.overlord.sramp.common.repository.PersistenceManager#updateOntology(org.overlord.sramp.common.ontology.SrampOntology)
	 */
	@Override
	public void updateOntology(SrampOntology ontology) throws SrampException {
		Session session = null;
		String ontologyPath = "/s-ramp/ontology/" + ontology.getUuid();

		try {
			session = JCRRepositoryFactory.getSession();
			if (session.nodeExists(ontologyPath)) {
				Node ontologyNode = session.getNode(ontologyPath);
				NodeIterator nodes = ontologyNode.getNodes();
				while (nodes.hasNext()) {
					Node child = nodes.nextNode();
					child.remove();
				}
				o2jcr.write(ontology, ontologyNode);
			} else {
                throw new OntologyNotFoundException(ontology.getUuid());
			}
			log.debug("Successfully updated ontology {}.", ontology.getUuid());
            session.getWorkspace().getObservationManager().setUserData(JCRAuditConstants.AUDIT_BUNDLE_ONTOLOGY_UPDATED);
			session.save();
        } catch (SrampException se) {
            throw se;
        } catch (Throwable t) {
            throw new SrampServerException(t);
		} finally {
			JCRRepositoryFactory.logoutQuietly(session);
		}
	}

	/**
	 * @see org.overlord.sramp.common.repository.PersistenceManager#deleteOntology(java.lang.String)
	 */
	@Override
	public void deleteOntology(String uuid) throws SrampException {
		Session session = null;
		String ontologyPath = "/s-ramp/ontology/" + uuid;

		try {
			session = JCRRepositoryFactory.getSession();
			if (session.nodeExists(ontologyPath)) {
				Node ontologyNode = session.getNode(ontologyPath);
				ontologyNode.remove();
			} else {
                throw new OntologyNotFoundException(uuid);
			}
            session.getWorkspace().getObservationManager().setUserData(JCRAuditConstants.AUDIT_BUNDLE_ONTOLOGY_DELETED);
			session.save();
			log.debug("Successfully deleted ontology {}.", uuid);
        } catch (SrampException se) {
            throw se;
        } catch (Throwable t) {
            throw new SrampServerException(t);
		} finally {
			JCRRepositoryFactory.logoutQuietly(session);
		}
	}

	/**
	 * @see org.overlord.sramp.common.repository.jcr.ClassificationHelper#resolve(java.lang.String)
	 */
	@Override
	public URI resolve(String classifiedBy) throws SrampException {
		URI classifiedUri = null;
		try {
			classifiedUri = new URI(classifiedBy);
		} catch (URISyntaxException e) {
			throw new InvalidClassifiedByException(classifiedBy);
		}
		Collection<SrampOntology> ontologies = getOntologies();
		for (SrampOntology ontology : ontologies) {
			Class sclass = ontology.findClass(classifiedBy);
			if (sclass == null) {
				sclass = ontology.findClass(classifiedUri);
			}
			if (sclass != null) {
				return sclass.getUri();
			}
		}
        throw new InvalidClassifiedByException(classifiedBy);
	}

	/**
	 * @see org.overlord.sramp.common.repository.jcr.ClassificationHelper#normalize(java.net.URI)
	 */
	@Override
	public Collection<URI> normalize(URI classification) throws SrampException {
		List<SrampOntology> ontologies = getOntologies();
		for (SrampOntology ontology : ontologies) {
			Class sclass = ontology.findClass(classification);
			if (sclass != null) {
	            return sclass.normalize();
			}
		}
        throw new InvalidClassifiedByException(classification.toString());
	}

	/**
	 * @see org.overlord.sramp.common.repository.jcr.ClassificationHelper#resolveAll(java.util.Collection)
	 */
	@Override
	public Collection<URI> resolveAll(Collection<String> classifiedBy) throws SrampException {
		Set<URI> resolved = new HashSet<URI>(classifiedBy.size());
		for (String classification : classifiedBy) {
			resolved.add(resolve(classification));
		}
		return resolved;
	}

	/**
	 * @see org.overlord.sramp.common.repository.jcr.ClassificationHelper#normalizeAll(java.util.Collection)
	 */
	@Override
	public Collection<URI> normalizeAll(Collection<URI> classifications) throws SrampException {
		Set<URI> resolved = new HashSet<URI>(classifications.size());
		for (URI classification : classifications) {
			resolved.addAll(normalize(classification));
		}
		return resolved;
	}

	/**
	 * @see org.overlord.sramp.common.repository.PersistenceManager#printArtifactGraph(java.lang.String, org.overlord.sramp.common.ArtifactType)
	 */
	@Override
	public void printArtifactGraph(String uuid, ArtifactType type) {
		Session session = null;
		try {
			session = JCRRepositoryFactory.getSession();
			Node artifactNode = findArtifactNode(uuid, type, session);
			if (artifactNode != null) {
    			JCRUtils tools = new JCRUtils();
    			tools.printSubgraph(artifactNode);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JCRRepositoryFactory.logoutQuietly(session);
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

	/**
	 * @see org.overlord.sramp.common.repository.PersistenceManager#shutdown()
	 */
	@Override
	public void shutdown() {
		JCRRepositoryFactory.destroy();
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
		 * @see org.overlord.sramp.common.repository.jcr.mapper.ArtifactToJCRNodeVisitor.JCRReferenceFactory#createReference(java.lang.String)
		 */
		@Override
		public Value createReference(String uuid) throws SrampException {
            try {
                Node node = findArtifactNodeByUuid(session, uuid);
    			if (node == null) {
                    throw new ArtifactNotFoundException(uuid);
                }
				return session.getValueFactory().createValue(node, false);
            } catch (SrampException se) {
                throw se;
            } catch (Throwable t) {
                throw new SrampServerException(t);
            }
		}
	}

}
