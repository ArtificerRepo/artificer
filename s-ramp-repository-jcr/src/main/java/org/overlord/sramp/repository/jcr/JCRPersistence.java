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
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;

import org.apache.commons.io.IOUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DocumentArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.StoredQuery;
import org.overlord.sramp.common.ArtifactNotFoundException;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.common.InvalidArtifactUpdateException;
import org.overlord.sramp.common.SrampConfig;
import org.overlord.sramp.common.SrampException;
import org.overlord.sramp.common.SrampServerException;
import org.overlord.sramp.common.derived.ArtifactDeriver;
import org.overlord.sramp.common.derived.ArtifactDeriverFactory;
import org.overlord.sramp.common.derived.LinkerContext;
import org.overlord.sramp.common.ontology.InvalidClassifiedByException;
import org.overlord.sramp.common.ontology.OntologyAlreadyExistsException;
import org.overlord.sramp.common.ontology.OntologyNotFoundException;
import org.overlord.sramp.common.ontology.SrampOntology;
import org.overlord.sramp.common.ontology.SrampOntology.SrampOntologyClass;
import org.overlord.sramp.common.storedquery.StoredQueryAlreadyExistsException;
import org.overlord.sramp.common.storedquery.StoredQueryNotFoundException;
import org.overlord.sramp.common.visitors.ArtifactVisitorHelper;
import org.overlord.sramp.repository.DerivedArtifacts;
import org.overlord.sramp.repository.PersistenceManager;
import org.overlord.sramp.repository.jcr.JCRArtifactPersister.Phase1Result;
import org.overlord.sramp.repository.jcr.JCRArtifactPersister.Phase2Result;
import org.overlord.sramp.repository.jcr.audit.ArtifactJCRNodeDiffer;
import org.overlord.sramp.repository.jcr.i18n.Messages;
import org.overlord.sramp.repository.jcr.mapper.ArtifactToJCRNodeVisitor;
import org.overlord.sramp.repository.jcr.mapper.JCRNodeToOntology;
import org.overlord.sramp.repository.jcr.mapper.JCRNodeToStoredQuery;
import org.overlord.sramp.repository.jcr.mapper.OntologyToJCRNode;
import org.overlord.sramp.repository.jcr.mapper.StoredQueryToJCRNode;
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

@Component(name = "JCR Persistence", immediate = true)
@Service(value = { org.overlord.sramp.repository.DerivedArtifacts.class,
        org.overlord.sramp.repository.PersistenceManager.class })
public class JCRPersistence extends AbstractJCRManager implements PersistenceManager, DerivedArtifacts, ClassificationHelper {

	private static Logger log = LoggerFactory.getLogger(JCRPersistence.class);

	private static OntologyToJCRNode o2jcr = new OntologyToJCRNode();
	private static JCRNodeToOntology jcr2o = new JCRNodeToOntology();
    private static StoredQueryToJCRNode q2jcr = new StoredQueryToJCRNode();
    private static JCRNodeToStoredQuery jcr2q = new JCRNodeToStoredQuery();

	//	private Map<String, SrampOntology> ontologyCache = new HashMap<String, SrampOntology>();

	/**
	 * Default constructor.
	 */
	public JCRPersistence() {
	}

	/**
	 * @see org.overlord.sramp.repository.PersistenceManager#persistBatch(java.util.List)
	 */
	@Override
	public List<Object> persistBatch(List<BatchItem> items) throws SrampException {
	    List<Object> rval = new ArrayList<Object>(items.size());
        Session session = null;
        try {
            session = JCRRepositoryFactory.getSession();

            // For each item in the batch, do phase 1 of the persist.
            for (BatchItem item : items) {
                try {
                    Phase1Result phase1 = JCRArtifactPersister.persistArtifactPhase1(session, item.baseArtifactType, item.content, this);
                    // If this is a document artifact then we need to execute phases 2 and 3.  If it's not
                    // then we're done and we should simply store the artifact as the result.
                    if (phase1.isDocumentArtifact) {
                        item.attributes.put("phase1", phase1); //$NON-NLS-1$
                    } else {
                        BaseArtifactType artifact = JCRNodeToArtifactFactory.createArtifact(session, phase1.artifactNode, phase1.artifactType);
                        item.attributes.put("result", artifact); //$NON-NLS-1$
                    }
                } catch (Exception e) {
                    item.attributes.put("result", e); //$NON-NLS-1$
                }
            }

            // Next, do phase 2 for each item in the batch.
            for (BatchItem item : items) {
                try {
                    if (item.attributes.containsKey("phase1")) { //$NON-NLS-1$
                        Phase1Result phase1 = (Phase1Result) item.attributes.get("phase1"); //$NON-NLS-1$
                        if (phase1.isDocumentArtifact) {
                            Phase2Result phase2 = JCRArtifactPersister.persistArtifactPhase2(session, item.baseArtifactType, this, phase1);
                            item.attributes.put("phase2", phase2); //$NON-NLS-1$
                        }
                    }
                } catch (Exception e) {
                    item.attributes.put("result", e); //$NON-NLS-1$
                }
            }

            // Lastly, do phase 3 for each item in the batch.
            for (BatchItem item : items) {
                try {
                    if (item.attributes.containsKey("phase2")) { //$NON-NLS-1$
                        Phase1Result phase1 = (Phase1Result) item.attributes.get("phase1"); //$NON-NLS-1$
                        if (phase1.isDocumentArtifact) {
                            Phase2Result phase2 = (Phase2Result) item.attributes.get("phase2"); //$NON-NLS-1$
                            JCRArtifactPersister.persistArtifactPhase3(session, item.baseArtifactType, this, phase1, phase2);
                            BaseArtifactType artifact = JCRNodeToArtifactFactory.createArtifact(session, phase1.artifactNode, phase1.artifactType);
                            item.attributes.put("result", artifact); //$NON-NLS-1$
                        }
                    }
                } catch (Exception e) {
                    item.attributes.put("result", e); //$NON-NLS-1$
                }
            }

            // And return the appropriate value for each item
            for (BatchItem item : items) {
                rval.add(item.attributes.get("result")); //$NON-NLS-1$
            }
        } catch (Throwable t) {
            throw new SrampServerException(t);
        } finally {
            JCRRepositoryFactory.logoutQuietly(session);
        }
        return rval;
	}

	/**
	 * @see org.overlord.sramp.common.repository.PersistenceManager#persistArtifact(java.lang.String, org.overlord.sramp.common.ArtifactType, java.io.InputStream)
	 */
	@Override
	public BaseArtifactType persistArtifact(BaseArtifactType metaData, InputStream content) throws SrampException {
		Session session = null;
		try {
			session = JCRRepositoryFactory.getSession();
            Phase1Result phase1 = JCRArtifactPersister.persistArtifactPhase1(session, metaData, content, this);
			ArtifactType artifactType = phase1.artifactType;
			Node artifactNode = phase1.artifactNode;

            // Derive any content (this could modify the artifact currently being persisted *and*
            // create additional artifacts).  So when we're done, we need to save any potential
			// changes to the original artifact as well as persist any derived artifacts.  Only
			// do this for document style artifacts.
			if (phase1.isDocumentArtifact) {
			    Phase2Result phase2 = JCRArtifactPersister.persistArtifactPhase2(session, metaData, this, phase1);
			    JCRArtifactPersister.persistArtifactPhase3(session, metaData, this, phase1, phase2);
			}

			// If debug is enabled, print the artifact graph
			if (log.isDebugEnabled()) {
				printArtifactGraph(metaData.getUuid(), artifactType);
			}

			// Create the S-RAMP Artifact object from the JCR node
			return JCRNodeToArtifactFactory.createArtifact(session, artifactNode, artifactType);
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
			log.debug(Messages.i18n.format("SUCCESSFUL_DERIVATION", derivedArtifacts.size(), sourceArtifact.getUuid())); //$NON-NLS-1$
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
            log.debug(Messages.i18n.format("SUCCESSFUL_LINKAGE", derivedArtifacts.size(), sourceArtifact.getUuid())); //$NON-NLS-1$
        } catch (Exception e) {
            throw new SrampServerException(e);
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
			Node artifactContentNode = artifactNode.getNode("jcr:content"); //$NON-NLS-1$
			File tempFile = JCRArtifactPersister.saveToTempFile(artifactContentNode);
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
	public BaseArtifactType updateArtifact(BaseArtifactType artifact, ArtifactType type) throws SrampException {
		Session session = null;
		ArtifactJCRNodeDiffer differ = null;
		try {
			session = JCRRepositoryFactory.getSession();
			Node artifactNode = findArtifactNode(artifact.getUuid(), type, session);
			if (artifactNode == null) {
				throw new ArtifactNotFoundException(artifact.getUuid());
			}
			if (SrampConfig.isAuditingEnabled()) {
			    differ = new ArtifactJCRNodeDiffer(artifactNode);
			}
			ArtifactToJCRNodeVisitor visitor = new ArtifactToJCRNodeVisitor(type, artifactNode,
					new JCRReferenceFactoryImpl(session), this);
			ArtifactVisitorHelper.visitArtifact(visitor, artifact);
			if (visitor.hasError())
				throw visitor.getError();
			session.save();

			log.debug(Messages.i18n.format("UPDATED_ARTY_META_DATA", artifact.getUuid())); //$NON-NLS-1$

			if (log.isDebugEnabled()) {
				printArtifactGraph(artifact.getUuid(), type);
			}

			if (SrampConfig.isAuditingEnabled()) {
			    JCRArtifactPersister.auditUpdateArtifact(differ, artifactNode);
			    session.save();
			}
			
			return JCRNodeToArtifactFactory.createArtifact(session, artifactNode, type);
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
	public BaseArtifactType updateArtifactContent(String uuid, ArtifactType type, InputStream content) throws SrampException {
		Session session = null;
		try {
			session = JCRRepositoryFactory.getSession();
            Node artifactNode = findArtifactNode(uuid, type, session);
            if (artifactNode == null) {
                throw new ArtifactNotFoundException(uuid);
            }
            if (artifactNode.isNodeType(JCRConstants.SRAMP_NON_DOCUMENT_TYPE)) {
                throw new InvalidArtifactUpdateException(Messages.i18n.format("JCRPersistence.NoArtifactContent")); //$NON-NLS-1$
            }
			JCRUtils tools = new JCRUtils();
			tools.uploadFile(session, artifactNode.getPath(), content);
			JCRUtils.setArtifactContentMimeType(artifactNode, type.getMimeType());

			// Document
			if (DocumentArtifactType.class.isAssignableFrom(type.getArtifactType().getTypeClass())) {
				artifactNode.setProperty(JCRConstants.SRAMP_CONTENT_TYPE, type.getMimeType());
				artifactNode.setProperty(JCRConstants.SRAMP_CONTENT_SIZE, artifactNode.getProperty("jcr:content/jcr:data").getLength()); //$NON-NLS-1$
				// TODO also handle the hash here
			}

			// TODO delete and re-create the derived artifacts?  what if some of them have properties or classifications?
			// TODO is "update content" even allowed in s-ramp??

			session.save();
			log.debug(Messages.i18n.format("UPDATED_ARTY_CONTENT", uuid)); //$NON-NLS-1$
            
            return JCRNodeToArtifactFactory.createArtifact(session, artifactNode, type);
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
	public BaseArtifactType deleteArtifact(String uuid, ArtifactType type) throws SrampException {
		Session session = null;
		try {
			session = JCRRepositoryFactory.getSession();
            Node artifactNode = findArtifactNode(uuid, type, session);
            if (artifactNode == null) {
                throw new ArtifactNotFoundException(uuid);
            }
            
            // Move the node to the trash.
            String srcPath = artifactNode.getPath();
            String parentTrashPath = MapToJCRPath.getTrashPath(srcPath);
            // Append "/[timestamp]" to the trash path.  This allows users to deploy another artifact with the
            // same UUID, then delete it later on without "same name siblings" collisions in the trash.
            String trashPath = parentTrashPath + "/" + Calendar.getInstance().getTimeInMillis(); //$NON-NLS-1$

            JCRUtils jcrUtils = new JCRUtils();
            jcrUtils.findOrCreateNode(session, parentTrashPath, "nt:folder"); //$NON-NLS-1$
            // Move the jcr node
            session.move(srcPath, trashPath);
			session.save();
			log.debug(Messages.i18n.format("DELETED_ARTY", uuid)); //$NON-NLS-1$
			
			return JCRNodeToArtifactFactory.createArtifact(session, artifactNode, type);
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
        String ontologyPath = MapToJCRPath.getOntologyPath(ontology.getUuid());

        // Check if an ontology with the given base URL already exists.
        List<SrampOntology> ontologies = getOntologies();
        for (SrampOntology existingOntology : ontologies) {
            if (existingOntology.getBase().equals(ontology.getBase())) {
                throw new OntologyAlreadyExistsException();
            }
        }

        try {
            session = JCRRepositoryFactory.getSession();
            if (session.nodeExists(ontologyPath)) {
                throw new OntologyAlreadyExistsException(ontology.getUuid());
            } else {
                JCRUtils tools = new JCRUtils();
                Node ontologiesNode = tools.findOrCreateNode(session, "/s-ramp/ontologies", "nt:folder"); //$NON-NLS-1$ //$NON-NLS-2$
                Node ontologyNode = ontologiesNode.addNode(ontology.getUuid(), "sramp:ontology"); //$NON-NLS-1$
                o2jcr.write(ontology, ontologyNode);
                session.save();
                log.debug(Messages.i18n.format("SAVED_ONTOLOGY", ontology.getUuid())); //$NON-NLS-1$
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
        String ontologyPath = MapToJCRPath.getOntologyPath(uuid);

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
            Node ontologiesNode = tools.findOrCreateNode(session, "/s-ramp/ontologies", "nt:folder"); //$NON-NLS-1$ //$NON-NLS-2$
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
        String ontologyPath = MapToJCRPath.getOntologyPath(ontology.getUuid());

        try {
            session = JCRRepositoryFactory.getSession();
            if (session.nodeExists(ontologyPath)) {
                Node ontologyNode = session.getNode(ontologyPath);
                o2jcr.update(ontology, ontologyNode);
            } else {
                throw new OntologyNotFoundException(ontology.getUuid());
            }
            log.debug(Messages.i18n.format("UPDATED_ONTOLOGY", ontology.getUuid())); //$NON-NLS-1$
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
        String ontologyPath = MapToJCRPath.getOntologyPath(uuid);

        try {
            session = JCRRepositoryFactory.getSession();
            if (session.nodeExists(ontologyPath)) {
                Node ontologyNode = session.getNode(ontologyPath);
                ontologyNode.remove();
            } else {
                throw new OntologyNotFoundException(uuid);
            }
            session.save();
            log.debug(Messages.i18n.format("DELETED_ONTOLOGY", uuid)); //$NON-NLS-1$
        } catch (SrampException se) {
            throw se;
        } catch (Throwable t) {
            throw new SrampServerException(t);
        } finally {
            JCRRepositoryFactory.logoutQuietly(session);
        }
    }

    @Override
    public StoredQuery persistStoredQuery(StoredQuery storedQuery) throws SrampException {
        String name = storedQuery.getQueryName();
        Session session = null;
        String storedQueryPath = MapToJCRPath.getStoredQueryPath(name);

        // Check if a stored query with the given name already exists.
        try {
            getStoredQuery(storedQuery.getQueryName());
            throw new StoredQueryAlreadyExistsException(name);
        } catch (StoredQueryNotFoundException e) {
            // do nothing -- success
        }

        try {
            session = JCRRepositoryFactory.getSession();
            if (session.nodeExists(storedQueryPath)) {
                throw new StoredQueryAlreadyExistsException(name);
            } else {
                JCRUtils tools = new JCRUtils();
                Node queriesNode = tools.findOrCreateNode(session, "/s-ramp/queries", "nt:folder"); //$NON-NLS-1$ //$NON-NLS-2$
                Node queryNode = queriesNode.addNode(name, "sramp:query"); //$NON-NLS-1$
                q2jcr.write(storedQuery, queryNode);
                session.save();
                log.debug(Messages.i18n.format("SAVED_STOREDQUERY", name)); //$NON-NLS-1$
                return storedQuery;
            }
        } catch (SrampException se) {
            throw se;
        } catch (Throwable t) {
            throw new SrampServerException(t);
        } finally {
            JCRRepositoryFactory.logoutQuietly(session);
        }
    }

    @Override
    public StoredQuery getStoredQuery(String queryName) throws SrampException {
        Session session = null;
        String storedQueryPath = MapToJCRPath.getStoredQueryPath(queryName);

        try {
            session = JCRRepositoryFactory.getSession();
            if (session.nodeExists(storedQueryPath)) {
                Node queryNode = session.getNode(storedQueryPath);
                StoredQuery storedQuery = new StoredQuery();
                jcr2q.read(storedQuery, queryNode);
                return storedQuery;
            } else {
                throw new StoredQueryNotFoundException(queryName);
            }
        } catch (SrampException se) {
            throw se;
        } catch (Throwable t) {
            throw new SrampServerException(t);
        } finally {
            JCRRepositoryFactory.logoutQuietly(session);
        }
    }

    @Override
    public List<StoredQuery> getStoredQueries() throws SrampException {
        Session session = null;

        try {
            session = JCRRepositoryFactory.getSession();
            JCRUtils tools = new JCRUtils();
            Node queriesNode = tools.findOrCreateNode(session, "/s-ramp/queries", "nt:folder"); //$NON-NLS-1$ //$NON-NLS-2$
            NodeIterator nodes = queriesNode.getNodes();
            List<StoredQuery> storedQueries = new ArrayList<StoredQuery>();
            while (nodes.hasNext()) {
                Node queryNode = nodes.nextNode();
                StoredQuery storedQuery = new StoredQuery();
                jcr2q.read(storedQuery, queryNode);
                storedQueries.add(storedQuery);
            }
            return storedQueries;
        } catch (Throwable t) {
            throw new SrampServerException(t);
        } finally {
            JCRRepositoryFactory.logoutQuietly(session);
        }
    }

    @Override
    public void updateStoredQuery(String queryName, StoredQuery storedQuery) throws SrampException {
        Session session = null;
        String storedQueryPath = MapToJCRPath.getStoredQueryPath(queryName);

        try {
            session = JCRRepositoryFactory.getSession();
            if (session.nodeExists(storedQueryPath)) {
                Node queryNode = session.getNode(storedQueryPath);
                q2jcr.write(storedQuery, queryNode);
            } else {
                throw new StoredQueryNotFoundException(queryName);
            }
            log.debug(Messages.i18n.format("UPDATED_STOREDQUERY", queryName)); //$NON-NLS-1$
            session.save();
        } catch (SrampException se) {
            throw se;
        } catch (Throwable t) {
            throw new SrampServerException(t);
        } finally {
            JCRRepositoryFactory.logoutQuietly(session);
        }
    }

    @Override
    public void deleteStoredQuery(String queryName) throws SrampException {
        Session session = null;
        String storedQueryPath = MapToJCRPath.getStoredQueryPath(queryName);

        try {
            session = JCRRepositoryFactory.getSession();
            if (session.nodeExists(storedQueryPath)) {
                Node queryNode = session.getNode(storedQueryPath);
                queryNode.remove();
            } else {
                throw new StoredQueryNotFoundException(queryName);
            }
            session.save();
            log.debug(Messages.i18n.format("DELETED_STOREDQUERY", queryName)); //$NON-NLS-1$
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
			SrampOntologyClass sclass = ontology.findClass(classifiedBy);
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
			SrampOntologyClass sclass = ontology.findClass(classification);
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
     * @see org.overlord.sramp.repository.PersistenceManager#startup()
     */
    @Override
    public void startup() {
        try {
            JCRExtensions.getInstance().startup();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
	
	/**
	 * @see org.overlord.sramp.common.repository.PersistenceManager#shutdown()
	 */
	@Override
	public void shutdown() {
		JCRRepositoryFactory.destroy();
	}

}
