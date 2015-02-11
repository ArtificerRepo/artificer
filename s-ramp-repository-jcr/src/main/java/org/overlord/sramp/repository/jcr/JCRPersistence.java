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

import org.apache.commons.lang.StringUtils;
import org.modeshape.jcr.api.ServletCredentials;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.StoredQuery;
import org.overlord.sramp.common.ArtifactContent;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.common.SrampConfig;
import org.overlord.sramp.common.SrampException;
import org.overlord.sramp.common.error.ArtifactNotFoundException;
import org.overlord.sramp.common.error.InvalidArtifactUpdateException;
import org.overlord.sramp.common.error.SrampServerException;
import org.overlord.sramp.common.error.StoredQueryConflictException;
import org.overlord.sramp.common.error.StoredQueryNotFoundException;
import org.overlord.sramp.common.ontology.InvalidClassifiedByException;
import org.overlord.sramp.common.ontology.OntologyConflictException;
import org.overlord.sramp.common.ontology.OntologyNotFoundException;
import org.overlord.sramp.common.ontology.SrampOntology;
import org.overlord.sramp.common.ontology.SrampOntology.SrampOntologyClass;
import org.overlord.sramp.common.visitors.ArtifactVisitorHelper;
import org.overlord.sramp.repository.PersistenceManager;
import org.overlord.sramp.repository.jcr.audit.ArtifactJCRNodeDiffer;
import org.overlord.sramp.repository.jcr.i18n.Messages;
import org.overlord.sramp.repository.jcr.mapper.ArtifactToJCRNodeVisitor;
import org.overlord.sramp.repository.jcr.mapper.JCRNodeToOntology;
import org.overlord.sramp.repository.jcr.mapper.JCRNodeToStoredQuery;
import org.overlord.sramp.repository.jcr.mapper.OntologyToJCRNode;
import org.overlord.sramp.repository.jcr.mapper.StoredQueryToJCRNode;
import org.overlord.sramp.repository.jcr.util.DeleteOnCloseFileInputStream;
import org.overlord.sramp.repository.jcr.util.JCRArtifactConstraintUtil;
import org.overlord.sramp.repository.jcr.util.JCRUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
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

/**
 * A JCR-specific implementation of the {@link PersistenceManager} interface, providing a JCR backend implementation
 * of the S-RAMP repository.
 *
 * This particular implementation leverages the ModeShape sequencing feature to assist with the
 * creation of the S-RAMP derived artifacts.
 */

public class JCRPersistence extends JCRAbstractManager implements PersistenceManager, ClassificationHelper {

    private static Logger log = LoggerFactory.getLogger(JCRPersistence.class);

    private static OntologyToJCRNode o2jcr = new OntologyToJCRNode();
    private static JCRNodeToOntology jcr2o = new JCRNodeToOntology();
    private static StoredQueryToJCRNode q2jcr = new StoredQueryToJCRNode();
    private static JCRNodeToStoredQuery jcr2q = new JCRNodeToStoredQuery();

    //  private Map<String, SrampOntology> ontologyCache = new HashMap<String, SrampOntology>();

    /**
     * Default constructor.
     */
    public JCRPersistence() {
    }

    @Override
    public List<Object> persistBatch(List<BatchItem> items) throws SrampException {
        List<Object> rval = new ArrayList<Object>(items.size());
        Session session = null;
        try {
            session = JCRRepositoryFactory.getSession();

            // First, persist each item, *without* relationships.
            for (BatchItem item : items) {
                try {
                    JCRArtifactPersister persister = new JCRArtifactPersister(item.baseArtifactType, item.content, this, session);
                    persister.persistArtifact();
                    item.attributes.put("persister", persister);
                } catch (Exception e) {
                    item.attributes.put("result", e);
                }
            }

            // Then, persist all relationships.  Splitting up the steps allows the entire batch to have some context
            // for the relationship targets.
            for (BatchItem item : items) {
                try {
                    if (item.attributes.containsKey("persister")) {
                        JCRArtifactPersister persister = (JCRArtifactPersister) item.attributes.get("persister");
                        persister.persistArtifactRelationships();
                        BaseArtifactType artifact = JCRNodeToArtifactFactory.createArtifact(
                                session, persister.getPrimaryArtifactNode(), ArtifactType.valueOf(item.baseArtifactType));
                        item.attributes.put("result", artifact);
                    }
                } catch (Exception e) {
                    item.attributes.put("result", e);
                }
            }

            // And return the appropriate value for each item
            for (BatchItem item : items) {
                rval.add(item.attributes.get("result"));
            }
        } catch (Throwable t) {
            throw new SrampServerException(t);
        } finally {
            JCRRepositoryFactory.logoutQuietly(session);
        }
        return rval;
    }

    @Override
    public BaseArtifactType persistArtifact(BaseArtifactType primaryArtifact, ArtifactContent content)
            throws SrampException {
        Session session = null;
        try {
            session = JCRRepositoryFactory.getSession();

            JCRArtifactPersister persister = new JCRArtifactPersister(primaryArtifact, content, this, session);
            persister.persistArtifact();
            persister.persistArtifactRelationships();
            
            ArtifactType artifactType = ArtifactType.valueOf(primaryArtifact);

            // If debug is enabled, print the artifact graph
            if (log.isDebugEnabled()) {
                printArtifactGraph(primaryArtifact.getUuid(), artifactType);
            }

            // Create the S-RAMP Artifact object from the JCR node
            return JCRNodeToArtifactFactory.createArtifact(session, JCRUtils.findArtifactNode(
                    primaryArtifact.getUuid(), artifactType, session), artifactType);
        } catch (SrampException se) {
            throw se;
        } catch (Throwable t) {
            throw new SrampServerException(t);
        } finally {
            JCRRepositoryFactory.logoutQuietly(session);
        }
    }

    @Override
    public BaseArtifactType getArtifact(String uuid, ArtifactType type) throws SrampException {
        Session session = null;
        try {
            session = JCRRepositoryFactory.getSession();
            Node artifactNode = JCRUtils.findArtifactNode(uuid, type, session);
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

    @Override
    public InputStream getArtifactContent(String uuid, ArtifactType type) throws SrampException {
        Session session = null;

        try {
            session = JCRRepositoryFactory.getSession();

            Node artifactNode = JCRUtils.findArtifactNode(uuid, type, session);
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
            Node artifactContentNode = artifactNode.getNode(JCRConstants.JCR_CONTENT);
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

    @Override
    public BaseArtifactType updateArtifact(BaseArtifactType artifact, ArtifactType type) throws SrampException {
        Session session = null;
        ArtifactJCRNodeDiffer differ = null;
        try {
            session = JCRRepositoryFactory.getSession();

            Node artifactNode = JCRUtils.findArtifactNode(artifact.getUuid(), type, session);
            if (artifactNode == null) {
                throw new ArtifactNotFoundException(artifact.getUuid());
            }
            if (SrampConfig.isAuditingEnabled()) {
                differ = new ArtifactJCRNodeDiffer(artifactNode);
            }
            ArtifactToJCRNodeVisitor visitor = new ArtifactToJCRNodeVisitor(type, artifactNode,
                    new JCRReferenceFactoryImpl(session), this);
            ArtifactVisitorHelper.visitArtifact(visitor, artifact);
            visitor.throwError();
            session.save();

            log.debug(Messages.i18n.format("UPDATED_ARTY_META_DATA", artifact.getUuid()));

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

    @Override
    public BaseArtifactType updateArtifactContent(String uuid, ArtifactType type, ArtifactContent content) throws SrampException {
        Session session = null;
        try {
            session = JCRRepositoryFactory.getSession();

            Node artifactNode = JCRUtils.findArtifactNode(uuid, type, session);
            if (artifactNode == null) {
                throw new ArtifactNotFoundException(uuid);
            }
            if (artifactNode.isNodeType(JCRConstants.SRAMP_NON_DOCUMENT_TYPE)) {
                throw new InvalidArtifactUpdateException(Messages.i18n.format("JCRPersistence.NoArtifactContent"));
            }

            JCRArtifactConstraintUtil.relationshipConstraintsOnDerived(uuid, artifactNode, session);
            JCRArtifactConstraintUtil.customMetadataConstraintsOnDerived(uuid, artifactNode);

            // Delete the current derived artifacts
            JCRArtifactConstraintUtil.deleteDerivedArtifacts(artifactNode, session);

            // Re-persist (which re-generates the derived artifacts).
            BaseArtifactType primaryArtifact = JCRNodeToArtifactFactory.createArtifact(session, artifactNode, type);
            JCRArtifactPersister persister = new JCRArtifactPersister(primaryArtifact, content, this, session);
            persister.updateArtifactContent(artifactNode);
            persister.persistArtifactRelationships();

            // TODO: Audit?

            log.debug(Messages.i18n.format("UPDATED_ARTY_CONTENT", uuid));

            // Create the S-RAMP Artifact object from the JCR node
            return JCRNodeToArtifactFactory.createArtifact(session, JCRUtils.findArtifactNode(
                    primaryArtifact.getUuid(), type, session), type);
        } catch (SrampException se) {
            throw se;
        } catch (Throwable t) {
            throw new SrampServerException(t);
        } finally {
            JCRRepositoryFactory.logoutQuietly(session);
        }
    }

    @Override
    public BaseArtifactType deleteArtifact(String uuid, ArtifactType type) throws SrampException {
        Session session = null;
        try {
            session = JCRRepositoryFactory.getSession();

            Node artifactNode = JCRUtils.findArtifactNode(uuid, type, session);
            if (artifactNode == null) {
                throw new ArtifactNotFoundException(uuid);
            }

            JCRArtifactConstraintUtil.relationshipConstraints(uuid, artifactNode, session);
            JCRArtifactConstraintUtil.deleteDerivedRelationships(artifactNode, session);
            
            // Move the node to the trash.
            String srcPath = artifactNode.getPath();
            String parentTrashPath = MapToJCRPath.getTrashPath(srcPath);
            // Append "/[timestamp]" to the trash path.  This allows users to deploy another artifact with the
            // same UUID, then delete it later on without "same name siblings" collisions in the trash.
            String trashPath = parentTrashPath + "/" + Calendar.getInstance().getTimeInMillis();

            JCRUtils.findOrCreateNode(session, parentTrashPath, JCRConstants.NT_FOLDER);
            // Move the jcr node
            session.move(srcPath, trashPath);
            session.save();
            log.debug(Messages.i18n.format("DELETED_ARTY", uuid));
            
            return JCRNodeToArtifactFactory.createArtifact(session, artifactNode, type);
        } catch (SrampException se) {
            throw se;
        } catch (Throwable t) {
            throw new SrampServerException(t);
        } finally {
            JCRRepositoryFactory.logoutQuietly(session);
        }
    }

    @Override
    public BaseArtifactType deleteArtifactContent(String uuid, ArtifactType type) throws SrampException {
        Session session = null;
        try {
            session = JCRRepositoryFactory.getSession();

            Node artifactNode = JCRUtils.findArtifactNode(uuid, type, session);
            if (artifactNode == null) {
                throw new ArtifactNotFoundException(uuid);
            }
            if (artifactNode.isNodeType(JCRConstants.SRAMP_NON_DOCUMENT_TYPE)) {
                throw new InvalidArtifactUpdateException(Messages.i18n.format("JCRPersistence.NoArtifactContent"));
            }

            JCRArtifactConstraintUtil.relationshipConstraintsOnDerived(uuid, artifactNode, session);

            // Delete the current derived artifacts
            JCRArtifactConstraintUtil.deleteDerivedArtifacts(artifactNode, session);

            artifactNode = JCRUtils.findArtifactNode(uuid, type, session);

            // Note: Only "unset" the size and hash, but not the actual mime/content types.  Doing so has quite a few
            // complications elsewhere in the code.
            artifactNode.setProperty(JCRConstants.SRAMP_CONTENT_SIZE, 0);
            artifactNode.setProperty(JCRConstants.SRAMP_CONTENT_HASH, "");
            session.save();

            log.debug(Messages.i18n.format("DELETED_ARTY_CONTENT", uuid));

            // Create the S-RAMP Artifact object from the JCR node
            return JCRNodeToArtifactFactory.createArtifact(session, JCRUtils.findArtifactNode(
                    uuid, type, session), type);
        } catch (SrampException se) {
            throw se;
        } catch (Throwable t) {
            throw new SrampServerException(t);
        } finally {
            JCRRepositoryFactory.logoutQuietly(session);
        }
    }

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
                throw new OntologyConflictException();
            }
        }

        try {
            session = JCRRepositoryFactory.getSession();
            if (session.nodeExists(ontologyPath)) {
                throw new OntologyConflictException(ontology.getUuid());
            } else {
                Node ontologiesNode = JCRUtils.findOrCreateNode(session, "/s-ramp/ontologies", JCRConstants.NT_FOLDER);
                Node ontologyNode = ontologiesNode.addNode(ontology.getUuid(), JCRConstants.SRAMP_ONTOLOGY);
                o2jcr.write(ontology, ontologyNode);
                session.save();
                log.debug(Messages.i18n.format("SAVED_ONTOLOGY", ontology.getUuid()));
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

    @Override
    public SrampOntology getOntology(String uuid) throws SrampException {
        Session session = null;
        String ontologyPath = MapToJCRPath.getOntologyPath(uuid);

        try {
            SrampOntology ontology = null;
            session = JCRRepositoryFactory.getSession();
            Node ontologyNode = JCRUtils.findNode(ontologyPath, session);
            if (ontologyNode != null) {
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

    @Override
    public List<SrampOntology> getOntologies() throws SrampException {
        // TODO add caching based on the last modified date of the ontology node
        Session session = null;

        try {
            session = JCRRepositoryFactory.getSession();
            Node ontologiesNode = JCRUtils.findOrCreateNode(session, "/s-ramp/ontologies", JCRConstants.NT_FOLDER);
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

    @Override
    public void updateOntology(SrampOntology ontology) throws SrampException {
        Session session = null;
        String ontologyPath = MapToJCRPath.getOntologyPath(ontology.getUuid());

        try {
            session = JCRRepositoryFactory.getSession();
            Node ontologyNode = JCRUtils.findNode(ontologyPath, session);
            if (ontologyNode != null) {
                o2jcr.update(ontology, ontologyNode);
            } else {
                throw new OntologyNotFoundException(ontology.getUuid());
            }
            log.debug(Messages.i18n.format("UPDATED_ONTOLOGY", ontology.getUuid()));
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
    public void deleteOntology(String uuid) throws SrampException {
        Session session = null;
        String ontologyPath = MapToJCRPath.getOntologyPath(uuid);

        try {
            session = JCRRepositoryFactory.getSession();
            Node ontologyNode = JCRUtils.findNode(ontologyPath, session);
            if (ontologyNode != null) {
                ontologyNode.remove();
            } else {
                throw new OntologyNotFoundException(uuid);
            }
            session.save();
            log.debug(Messages.i18n.format("DELETED_ONTOLOGY", uuid));
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

        // Validate the name
        if (StringUtils.isBlank(name)) {
            throw new StoredQueryConflictException();
        }

        // Check if a stored query with the given name already exists.
        try {
            getStoredQuery(storedQuery.getQueryName());
            throw new StoredQueryConflictException(name);
        } catch (StoredQueryNotFoundException e) {
            // do nothing -- success
        }

        String storedQueryPath = MapToJCRPath.getStoredQueryPath(name);

        try {
            session = JCRRepositoryFactory.getSession();
            if (session.nodeExists(storedQueryPath)) {
                throw new StoredQueryConflictException(name);
            } else {
                Node queriesNode = JCRUtils.findOrCreateNode(session, "/s-ramp/queries", JCRConstants.NT_FOLDER);
                Node queryNode = queriesNode.addNode(name, JCRConstants.SRAMP_QUERY);
                q2jcr.write(storedQuery, queryNode);
                session.save();
                log.debug(Messages.i18n.format("SAVED_STOREDQUERY", name));
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
            Node queryNode = JCRUtils.findNode(storedQueryPath, session);
            if (queryNode != null) {
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
            Node queriesNode = JCRUtils.findOrCreateNode(session, "/s-ramp/queries", JCRConstants.NT_FOLDER);
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
            Node queryNode = JCRUtils.findNode(storedQueryPath, session);
            if (queryNode != null) {
                q2jcr.write(storedQuery, queryNode);
            } else {
                throw new StoredQueryNotFoundException(queryName);
            }
            log.debug(Messages.i18n.format("UPDATED_STOREDQUERY", queryName));
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
            Node queryNode = JCRUtils.findNode(storedQueryPath, session);
            if (queryNode != null) {
                queryNode.remove();
            } else {
                throw new StoredQueryNotFoundException(queryName);
            }
            session.save();
            log.debug(Messages.i18n.format("DELETED_STOREDQUERY", queryName));
        } catch (SrampException se) {
            throw se;
        } catch (Throwable t) {
            throw new SrampServerException(t);
        } finally {
            JCRRepositoryFactory.logoutQuietly(session);
        }
    }

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

    @Override
    public Collection<URI> resolveAll(Collection<String> classifiedBy) throws SrampException {
        Set<URI> resolved = new HashSet<URI>(classifiedBy.size());
        for (String classification : classifiedBy) {
            resolved.add(resolve(classification));
        }
        return resolved;
    }

    @Override
    public Collection<URI> normalizeAll(Collection<URI> classifications) throws SrampException {
        Set<URI> resolved = new HashSet<URI>(classifications.size());
        for (URI classification : classifications) {
            resolved.addAll(normalize(classification));
        }
        return resolved;
    }

    @Override
    public void printArtifactGraph(String uuid, ArtifactType type) {
        Session session = null;
        try {
            session = JCRRepositoryFactory.getSession();
            Node artifactNode = JCRUtils.findArtifactNode(uuid, type, session);
            if (artifactNode != null) {
                JCRUtils.printSubgraph(artifactNode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JCRRepositoryFactory.logoutQuietly(session);
        }
    }

    @Override
    public void startup() {
        try {
            // Set credentials (manufactured for full privileges)
            HttpServletRequest request = new JCRStartupHttpServletRequest();
            ServletCredentials credentials = new ServletCredentials((HttpServletRequest) request);
            JCRRepositoryFactory.setLoginCredentials(credentials);

            try {
                JCRRepositoryFactory.getSession();
            } finally {
                JCRRepositoryFactory.clearLoginCredentials();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public void shutdown() {
        JCRRepositoryFactory.destroy();
    }

}
