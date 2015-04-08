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
package org.artificer.repository.jcr;

import org.apache.commons.lang.StringUtils;
import org.artificer.common.ArtifactContent;
import org.artificer.common.ArtifactType;
import org.artificer.common.ArtificerConfig;
import org.artificer.common.ArtificerException;
import org.artificer.common.error.ArtificerConflictException;
import org.artificer.common.error.ArtificerNotFoundException;
import org.artificer.common.error.ArtificerServerException;
import org.artificer.common.error.ArtificerUserException;
import org.artificer.common.ontology.ArtificerOntology;
import org.artificer.common.ontology.ArtificerOntology.ArtificerOntologyClass;
import org.artificer.common.visitors.ArtifactVisitorHelper;
import org.artificer.repository.PersistenceManager;
import org.artificer.repository.jcr.audit.ArtifactJCRNodeDiffer;
import org.artificer.repository.jcr.i18n.Messages;
import org.artificer.repository.jcr.mapper.ArtifactToJCRNodeVisitor;
import org.artificer.repository.jcr.mapper.JCRNodeToOntology;
import org.artificer.repository.jcr.mapper.JCRNodeToStoredQuery;
import org.artificer.repository.jcr.mapper.OntologyToJCRNode;
import org.artificer.repository.jcr.mapper.StoredQueryToJCRNode;
import org.artificer.repository.jcr.util.DeleteOnCloseFileInputStream;
import org.artificer.repository.jcr.util.JCRUtils;
import org.modeshape.jcr.api.ServletCredentials;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.StoredQuery;
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
 * A JCR-specific implementation of the {@link org.artificer.repository.PersistenceManager} interface, providing a JCR backend implementation
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
    public List<Object> persistBatch(List<BatchItem> items) throws ArtificerException {
        List<Object> rval = new ArrayList<Object>(items.size());
        Session session = null;
        try {
            session = JCRRepositoryFactory.getSession();
            // use a single JCRReferenceFactory for the entire batch
            ArtifactToJCRNodeVisitor.JCRReferenceFactory jcrReferenceFactory = new JCRReferenceFactoryImpl(session);

            // First, persist each item, *without* relationships.
            for (BatchItem item : items) {
                try {
                    JCRArtifactPersister persister = new JCRArtifactPersister(
                            item.baseArtifactType, item.content, this, jcrReferenceFactory, session);
                    persister.persistArtifact();
                    item.attributes.put("persister", persister);
                } catch (Exception e) {
                    item.attributes.put("result", e);
                }
            }

            // Save so that necessary artifacts are persisted for relationship lookups, below.
            session.save();

            // Then, persist all relationships.  Splitting up the steps allows the entire batch to have some context
            // for the relationship targets.
            for (BatchItem item : items) {
                try {
                    if (item.attributes.containsKey("persister")) {
                        JCRArtifactPersister persister = (JCRArtifactPersister) item.attributes.get("persister");
                        persister.persistArtifactRelationships();
                        item.attributes.put("result", persister.getPrimaryArtifactNode());
                    }
                } catch (Exception e) {
                    item.attributes.put("result", e);
                }
            }

            session.save();

            // And return the appropriate value for each item
            for (BatchItem item : items) {
                if (item.attributes.get("result") instanceof Node) {
                    Node node = (Node) item.attributes.get("result");
                    // It's important to do this *after* save, rather than before it (when the node is first added
                    // to the "result" attribute, above).  We rely on several ModeShape default values (created by,
                    // creation time, etc.).  Rather than try to fill those out on our own, delay until saved.
                    rval.add(JCRNodeToArtifactFactory.createArtifact(
                            session, node, ArtifactType.valueOf(item.baseArtifactType)));
                } else {
                    rval.add(item.attributes.get("result"));
                }
            }
        } catch (Throwable t) {
            throw new ArtificerServerException(t);
        } finally {
            JCRRepositoryFactory.logoutQuietly(session);
        }
        return rval;
    }

    @Override
    public BaseArtifactType persistArtifact(BaseArtifactType primaryArtifact, ArtifactContent content)
            throws ArtificerException {
        Session session = null;
        try {
            session = JCRRepositoryFactory.getSession();

            JCRArtifactPersister persister = new JCRArtifactPersister(primaryArtifact, content, this, session);
            persister.persistArtifact();
            persister.persistArtifactRelationships();
            
            ArtifactType artifactType = ArtifactType.valueOf(primaryArtifact);

            // If debug is enabled, print the artifact graph
            if (log.isDebugEnabled()) {
                JCRUtils.printSubgraph(persister.getPrimaryArtifactNode());
            }

            session.save();

            // Create the S-RAMP Artifact object from the JCR node
            return JCRNodeToArtifactFactory.createArtifact(session, persister.getPrimaryArtifactNode(), artifactType);
        } catch (ArtificerException se) {
            throw se;
        } catch (Throwable t) {
            throw new ArtificerServerException(t);
        } finally {
            JCRRepositoryFactory.logoutQuietly(session);
        }
    }

    @Override
    public BaseArtifactType getArtifact(String uuid, ArtifactType type) throws ArtificerException {
        Session session = null;
        try {
            session = JCRRepositoryFactory.getSession();
            Node artifactNode = JCRUtils.findArtifactNode(uuid, type, session);
            if (artifactNode != null) {
                // Create an artifact from the sequenced node
                return JCRNodeToArtifactFactory.createArtifact(session, artifactNode, type);
            } else {
                return null;
            }
        } catch (ArtificerException se) {
            throw se;
        } catch (Throwable t) {
            throw new ArtificerServerException(t);
        } finally {
            JCRRepositoryFactory.logoutQuietly(session);
        }
    }

    @Override
    public InputStream getArtifactContent(String uuid, ArtifactType type) throws ArtificerException {
        Session session = null;

        try {
            session = JCRRepositoryFactory.getSession();

            Node artifactNode = JCRUtils.findArtifactNode(uuid, type, session);
            if (artifactNode == null) {
                throw ArtificerNotFoundException.artifactNotFound(uuid);
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
        } catch (ArtificerException se) {
            throw se;
        } catch (Throwable t) {
            throw new ArtificerServerException(t);
        } finally {
            JCRRepositoryFactory.logoutQuietly(session);
        }
    }

    @Override
    public BaseArtifactType updateArtifact(BaseArtifactType artifact, ArtifactType type) throws ArtificerException {
        Session session = null;
        ArtifactJCRNodeDiffer differ = null;
        try {
            session = JCRRepositoryFactory.getSession();

            Node artifactNode = JCRUtils.findArtifactNode(artifact.getUuid(), type, session);
            if (artifactNode == null) {
                throw ArtificerNotFoundException.artifactNotFound(artifact.getUuid());
            }
            if (ArtificerConfig.isAuditingEnabled()) {
                differ = new ArtifactJCRNodeDiffer(artifactNode);
            }
            ArtifactToJCRNodeVisitor visitor = new ArtifactToJCRNodeVisitor(type, artifactNode,
                    new JCRReferenceFactoryImpl(session), this);
            ArtifactVisitorHelper.visitArtifact(visitor, artifact);
            visitor.throwError();

            log.debug(Messages.i18n.format("UPDATED_ARTY_META_DATA", artifact.getUuid()));

            if (log.isDebugEnabled()) {
                JCRUtils.printSubgraph(artifactNode);
            }

            if (ArtificerConfig.isAuditingEnabled()) {
                JCRArtifactPersister.auditUpdateArtifact(differ, artifactNode);
            }

            session.save();

            return JCRNodeToArtifactFactory.createArtifact(session, artifactNode, type);
        } catch (ArtificerException se) {
            throw se;
        } catch (Throwable t) {
            throw new ArtificerServerException(t);
        } finally {
            JCRRepositoryFactory.logoutQuietly(session);
        }
    }

    @Override
    public BaseArtifactType updateArtifactContent(String uuid, ArtifactType type, ArtifactContent content) throws ArtificerException {
        Session session = null;
        try {
            session = JCRRepositoryFactory.getSession();

            Node artifactNode = JCRUtils.findArtifactNode(uuid, type, session);
            if (artifactNode == null) {
                throw ArtificerNotFoundException.artifactNotFound(uuid);
            }
            if (artifactNode.isNodeType(JCRConstants.SRAMP_NON_DOCUMENT_TYPE)) {
                throw new ArtificerUserException(Messages.i18n.format("JCRPersistence.NoArtifactContent"));
            }

            JCRUtils.relationshipConstraintsOnDerived(uuid, artifactNode, session);
            JCRUtils.customMetadataConstraintsOnDerived(uuid, artifactNode);

            // Delete the current derived artifacts
            JCRUtils.deleteDerivedArtifacts(artifactNode, session);

            // Re-persist (which re-generates the derived artifacts).
            BaseArtifactType primaryArtifact = JCRNodeToArtifactFactory.createArtifact(session, artifactNode, type);
            JCRArtifactPersister persister = new JCRArtifactPersister(primaryArtifact, content, this, session);
            persister.updateArtifactContent(artifactNode);
            persister.persistArtifactRelationships();

            // TODO: Audit?

            session.save();

            log.debug(Messages.i18n.format("UPDATED_ARTY_CONTENT", uuid));

            // Create the S-RAMP Artifact object from the JCR node
            return JCRNodeToArtifactFactory.createArtifact(session, persister.getPrimaryArtifactNode(), type);
        } catch (ArtificerException se) {
            throw se;
        } catch (Throwable t) {
            throw new ArtificerServerException(t);
        } finally {
            JCRRepositoryFactory.logoutQuietly(session);
        }
    }

    @Override
    public BaseArtifactType addComment(String uuid, ArtifactType type, String text) throws ArtificerException {
        Session session = null;
        try {
            session = JCRRepositoryFactory.getSession();

            Node artifactNode = JCRUtils.findArtifactNode(uuid, type, session);
            if (artifactNode == null) {
                throw ArtificerNotFoundException.artifactNotFound(uuid);
            }

            // Need a unique node name, but don't really care about what it is specifically.  Use a UUID...
            String nodeName = JCRConstants.ARTIFICER_COMMENTS + ":" + UUID.randomUUID().toString();
            Node commentNode = artifactNode.addNode(nodeName, JCRConstants.ARTIFICER_COMMENT);
            commentNode.setProperty(JCRConstants.ARTIFICER_TEXT, text);

            session.save();

            // Create the S-RAMP Artifact object from the JCR node
            return JCRNodeToArtifactFactory.createArtifact(session, artifactNode, type);
        } catch (ArtificerException se) {
            throw se;
        } catch (Throwable t) {
            throw new ArtificerServerException(t);
        } finally {
            JCRRepositoryFactory.logoutQuietly(session);
        }
    }

    @Override
    public BaseArtifactType deleteArtifact(String uuid, ArtifactType type) throws ArtificerException {
        Session session = null;
        try {
            session = JCRRepositoryFactory.getSession();

            Node artifactNode = JCRUtils.findArtifactNode(uuid, type, session);
            if (artifactNode == null) {
                throw ArtificerNotFoundException.artifactNotFound(uuid);
            }

            JCRUtils.relationshipConstraints(uuid, artifactNode, session);
            JCRUtils.deleteDerivedRelationships(artifactNode, session);
            
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
        } catch (ArtificerException se) {
            throw se;
        } catch (Throwable t) {
            throw new ArtificerServerException(t);
        } finally {
            JCRRepositoryFactory.logoutQuietly(session);
        }
    }

    @Override
    public BaseArtifactType deleteArtifactContent(String uuid, ArtifactType type) throws ArtificerException {
        Session session = null;
        try {
            session = JCRRepositoryFactory.getSession();

            Node artifactNode = JCRUtils.findArtifactNode(uuid, type, session);
            if (artifactNode == null) {
                throw ArtificerNotFoundException.artifactNotFound(uuid);
            }
            if (artifactNode.isNodeType(JCRConstants.SRAMP_NON_DOCUMENT_TYPE)) {
                throw new ArtificerUserException(Messages.i18n.format("JCRPersistence.NoArtifactContent"));
            }

            JCRUtils.relationshipConstraintsOnDerived(uuid, artifactNode, session);

            // Delete the current derived artifacts
            JCRUtils.deleteDerivedArtifacts(artifactNode, session);

            // Note: Only "unset" the size and hash, but not the actual mime/content types.  Doing so has quite a few
            // complications elsewhere in the code.
            artifactNode.setProperty(JCRConstants.SRAMP_CONTENT_SIZE, 0);
            artifactNode.setProperty(JCRConstants.SRAMP_CONTENT_HASH, "");
            session.save();

            log.debug(Messages.i18n.format("DELETED_ARTY_CONTENT", uuid));

            // Create the S-RAMP Artifact object from the JCR node
            return JCRNodeToArtifactFactory.createArtifact(session, artifactNode, type);
        } catch (ArtificerException se) {
            throw se;
        } catch (Throwable t) {
            throw new ArtificerServerException(t);
        } finally {
            JCRRepositoryFactory.logoutQuietly(session);
        }
    }

    @Override
    public ArtificerOntology persistOntology(ArtificerOntology ontology) throws ArtificerException {
        Session session = null;
        if (ontology.getUuid() == null) {
            ontology.setUuid(UUID.randomUUID().toString());
        }
        String ontologyPath = MapToJCRPath.getOntologyPath(ontology.getUuid());

        // Check if an ontology with the given base URL already exists.
        List<ArtificerOntology> ontologies = getOntologies();
        for (ArtificerOntology existingOntology : ontologies) {
            if (existingOntology.getBase().equals(ontology.getBase())) {
                throw ArtificerConflictException.ontologyConflict(ontology.getUuid());
            }
        }

        try {
            session = JCRRepositoryFactory.getSession();
            if (session.nodeExists(ontologyPath)) {
                throw ArtificerConflictException.ontologyConflict(ontology.getUuid());
            } else {
                Node ontologiesNode = JCRUtils.findOrCreateNode(session, "/s-ramp/ontologies", JCRConstants.NT_FOLDER);
                Node ontologyNode = ontologiesNode.addNode(ontology.getUuid(), JCRConstants.SRAMP_ONTOLOGY);
                o2jcr.write(ontology, ontologyNode);
                session.save();
                log.debug(Messages.i18n.format("SAVED_ONTOLOGY", ontology.getUuid()));
                return ontology;
            }
        } catch (ArtificerException se) {
            throw se;
        } catch (Throwable t) {
            throw new ArtificerServerException(t);
        } finally {
            JCRRepositoryFactory.logoutQuietly(session);
        }
    }

    @Override
    public ArtificerOntology getOntology(String uuid) throws ArtificerException {
        Session session = null;
        String ontologyPath = MapToJCRPath.getOntologyPath(uuid);

        try {
            ArtificerOntology ontology = null;
            session = JCRRepositoryFactory.getSession();
            Node ontologyNode = JCRUtils.findNode(ontologyPath, session);
            if (ontologyNode != null) {
                ontology = new ArtificerOntology();
                ontology.setUuid(uuid);
                jcr2o.read(ontology, ontologyNode);
            } else {
                throw ArtificerNotFoundException.ontologyNotFound(uuid);
            }
            return ontology;
        } catch (ArtificerException se) {
            throw se;
        } catch (Throwable t) {
            throw new ArtificerServerException(t);
        } finally {
            JCRRepositoryFactory.logoutQuietly(session);
        }
    }

    @Override
    public List<ArtificerOntology> getOntologies() throws ArtificerException {
        // TODO add caching based on the last modified date of the ontology node
        Session session = null;

        try {
            session = JCRRepositoryFactory.getSession();
            Node ontologiesNode = JCRUtils.findOrCreateNode(session, "/s-ramp/ontologies", JCRConstants.NT_FOLDER);
            NodeIterator nodes = ontologiesNode.getNodes();
            List<ArtificerOntology> ontologies = new ArrayList<ArtificerOntology>();
            while (nodes.hasNext()) {
                Node node = nodes.nextNode();
                ArtificerOntology ontology = new ArtificerOntology();
                jcr2o.read(ontology, node);
                ontologies.add(ontology);
            }
            return ontologies;
        } catch (Throwable t) {
            throw new ArtificerServerException(t);
        } finally {
            JCRRepositoryFactory.logoutQuietly(session);
        }
    }

    @Override
    public void updateOntology(ArtificerOntology ontology) throws ArtificerException {
        Session session = null;
        String ontologyPath = MapToJCRPath.getOntologyPath(ontology.getUuid());

        try {
            session = JCRRepositoryFactory.getSession();
            Node ontologyNode = JCRUtils.findNode(ontologyPath, session);
            if (ontologyNode != null) {
                o2jcr.update(ontology, ontologyNode);
            } else {
                throw ArtificerNotFoundException.ontologyNotFound(ontology.getUuid());
            }
            log.debug(Messages.i18n.format("UPDATED_ONTOLOGY", ontology.getUuid()));
            session.save();
        } catch (ArtificerException se) {
            throw se;
        } catch (Throwable t) {
            throw new ArtificerServerException(t);
        } finally {
            JCRRepositoryFactory.logoutQuietly(session);
        }
    }

    @Override
    public void deleteOntology(String uuid) throws ArtificerException {
        Session session = null;
        String ontologyPath = MapToJCRPath.getOntologyPath(uuid);

        try {
            session = JCRRepositoryFactory.getSession();
            Node ontologyNode = JCRUtils.findNode(ontologyPath, session);
            if (ontologyNode != null) {
                ontologyNode.remove();
            } else {
                throw ArtificerNotFoundException.ontologyNotFound(uuid);
            }
            session.save();
            log.debug(Messages.i18n.format("DELETED_ONTOLOGY", uuid));
        } catch (ArtificerException se) {
            throw se;
        } catch (Throwable t) {
            throw new ArtificerServerException(t);
        } finally {
            JCRRepositoryFactory.logoutQuietly(session);
        }
    }

    @Override
    public StoredQuery persistStoredQuery(StoredQuery storedQuery) throws ArtificerException {
        String name = storedQuery.getQueryName();
        Session session = null;

        // Validate the name
        if (StringUtils.isBlank(name)) {
            throw ArtificerConflictException.storedQueryConflict();
        }

        // Check if a stored query with the given name already exists.
        try {
            getStoredQuery(storedQuery.getQueryName());
            throw ArtificerConflictException.storedQueryConflict(name);
        } catch (ArtificerNotFoundException e) {
            // do nothing -- success
        }

        String storedQueryPath = MapToJCRPath.getStoredQueryPath(name);

        try {
            session = JCRRepositoryFactory.getSession();
            if (session.nodeExists(storedQueryPath)) {
                throw ArtificerConflictException.storedQueryConflict(name);
            } else {
                Node queriesNode = JCRUtils.findOrCreateNode(session, "/s-ramp/queries", JCRConstants.NT_FOLDER);
                Node queryNode = queriesNode.addNode(name, JCRConstants.SRAMP_QUERY);
                q2jcr.write(storedQuery, queryNode);
                session.save();
                log.debug(Messages.i18n.format("SAVED_STOREDQUERY", name));
                return storedQuery;
            }
        } catch (ArtificerException se) {
            throw se;
        } catch (Throwable t) {
            throw new ArtificerServerException(t);
        } finally {
            JCRRepositoryFactory.logoutQuietly(session);
        }
    }

    @Override
    public StoredQuery getStoredQuery(String queryName) throws ArtificerException {
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
                throw ArtificerNotFoundException.storedQueryNotFound(queryName);
            }
        } catch (ArtificerException se) {
            throw se;
        } catch (Throwable t) {
            throw new ArtificerServerException(t);
        } finally {
            JCRRepositoryFactory.logoutQuietly(session);
        }
    }

    @Override
    public List<StoredQuery> getStoredQueries() throws ArtificerException {
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
            throw new ArtificerServerException(t);
        } finally {
            JCRRepositoryFactory.logoutQuietly(session);
        }
    }

    @Override
    public void updateStoredQuery(String queryName, StoredQuery storedQuery) throws ArtificerException {
        Session session = null;
        String storedQueryPath = MapToJCRPath.getStoredQueryPath(queryName);

        try {
            session = JCRRepositoryFactory.getSession();
            Node queryNode = JCRUtils.findNode(storedQueryPath, session);
            if (queryNode != null) {
                q2jcr.write(storedQuery, queryNode);
            } else {
                throw ArtificerNotFoundException.storedQueryNotFound(queryName);
            }
            log.debug(Messages.i18n.format("UPDATED_STOREDQUERY", queryName));
            session.save();
        } catch (ArtificerException se) {
            throw se;
        } catch (Throwable t) {
            throw new ArtificerServerException(t);
        } finally {
            JCRRepositoryFactory.logoutQuietly(session);
        }
    }

    @Override
    public void deleteStoredQuery(String queryName) throws ArtificerException {
        Session session = null;
        String storedQueryPath = MapToJCRPath.getStoredQueryPath(queryName);

        try {
            session = JCRRepositoryFactory.getSession();
            Node queryNode = JCRUtils.findNode(storedQueryPath, session);
            if (queryNode != null) {
                queryNode.remove();
            } else {
                throw ArtificerNotFoundException.storedQueryNotFound(queryName);
            }
            session.save();
            log.debug(Messages.i18n.format("DELETED_STOREDQUERY", queryName));
        } catch (ArtificerException se) {
            throw se;
        } catch (Throwable t) {
            throw new ArtificerServerException(t);
        } finally {
            JCRRepositoryFactory.logoutQuietly(session);
        }
    }

    @Override
    public URI resolve(String classifiedBy) throws ArtificerException {
        URI classifiedUri = null;
        try {
            classifiedUri = new URI(classifiedBy);
        } catch (URISyntaxException e) {
            throw ArtificerUserException.invalidClassifiedBy(classifiedBy);
        }
        Collection<ArtificerOntology> ontologies = getOntologies();
        for (ArtificerOntology ontology : ontologies) {
            ArtificerOntologyClass sclass = ontology.findClass(classifiedBy);
            if (sclass == null) {
                sclass = ontology.findClass(classifiedUri);
            }
            if (sclass != null) {
                return sclass.getUri();
            }
        }
        throw ArtificerUserException.invalidClassifiedBy(classifiedBy);
    }

    @Override
    public Collection<URI> normalize(URI classification) throws ArtificerException {
        List<ArtificerOntology> ontologies = getOntologies();
        for (ArtificerOntology ontology : ontologies) {
            ArtificerOntologyClass sclass = ontology.findClass(classification);
            if (sclass != null) {
                return sclass.normalize();
            }
        }
        throw ArtificerUserException.invalidClassifiedBy(classification.toString());
    }

    @Override
    public Collection<URI> resolveAll(Collection<String> classifiedBy) throws ArtificerException {
        Set<URI> resolved = new HashSet<URI>(classifiedBy.size());
        for (String classification : classifiedBy) {
            resolved.add(resolve(classification));
        }
        return resolved;
    }

    @Override
    public Collection<URI> normalizeAll(Collection<URI> classifications) throws ArtificerException {
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
