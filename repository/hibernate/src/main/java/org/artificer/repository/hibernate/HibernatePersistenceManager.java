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
package org.artificer.repository.hibernate;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.artificer.common.ArtifactContent;
import org.artificer.common.ArtifactType;
import org.artificer.common.ArtificerConfig;
import org.artificer.common.ArtificerException;
import org.artificer.common.error.ArtificerConflictException;
import org.artificer.common.error.ArtificerNotFoundException;
import org.artificer.common.error.ArtificerServerException;
import org.artificer.common.ontology.ArtificerOntology;
import org.artificer.common.ontology.ArtificerOntologyClass;
import org.artificer.integration.ExtensionFactory;
import org.artificer.integration.artifactbuilder.ArtifactBuilder;
import org.artificer.integration.artifactbuilder.RelationshipContext;
import org.artificer.repository.AbstractPersistenceManager;
import org.artificer.repository.ClassificationHelper;
import org.artificer.repository.hibernate.audit.HibernateAuditor;
import org.artificer.repository.hibernate.audit.ArtificerAuditEntry;
import org.artificer.repository.hibernate.data.HibernateEntityToSrampVisitor;
import org.artificer.repository.hibernate.data.SrampToHibernateEntityRelationshipsVisitor;
import org.artificer.repository.hibernate.data.SrampToHibernateEntityVisitor;
import org.artificer.repository.hibernate.entity.ArtificerArtifact;
import org.artificer.repository.hibernate.entity.ArtificerComment;
import org.artificer.repository.hibernate.entity.ArtificerDocumentArtifact;
import org.artificer.repository.hibernate.entity.ArtificerRelationship;
import org.artificer.repository.hibernate.entity.ArtificerRelationshipType;
import org.artificer.repository.hibernate.entity.ArtificerStoredQuery;
import org.artificer.repository.hibernate.file.FileManagerFactory;
import org.hibernate.Hibernate;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.StoredQuery;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Brett Meyer.
 */
public class HibernatePersistenceManager extends AbstractPersistenceManager {

    private final ClassificationHelper classificationHelper = this;

    @Override
    public void login(String username, String password) {
        // not used
    }

    @Override
    public List<Object> persistBatch(List<BatchItem> items) throws ArtificerException {
        List<Object> rval = new ArrayList<Object>(items.size());
        for (BatchItem item : items) {
            try {
                BaseArtifactType artifact = persistArtifact(item.baseArtifactType, item.content);
                rval.add(artifact);
            } catch (Throwable t) {
                rval.add(t);
            }
        }
        return rval;
    }

    @Override
    public BaseArtifactType persistArtifact(final BaseArtifactType srampArtifact, final ArtifactContent content)
            throws ArtificerException {
        final ArtifactType artifactType = ArtifactType.valueOf(srampArtifact);

        try {
            ArtificerArtifact artificerArtifact = new HibernateUtil.HibernateTask<ArtificerArtifact>() {
                @Override
                protected ArtificerArtifact doExecute(EntityManager entityManager) throws Exception {
                    if (StringUtils.isBlank(srampArtifact.getUuid())) {
                        srampArtifact.setUuid(UUID.randomUUID().toString());
                    } else {
                        // TODO: ugh -- ugly
                        try {
                            HibernateUtil.getArtifact(srampArtifact.getUuid(), entityManager, false);
                            throw ArtificerConflictException.artifactConflict(srampArtifact.getUuid());
                        } catch (ArtificerNotFoundException e) {
                            // do nothing
                        }
                    }

                    HibernateRelationshipFactory relationshipFactory = new HibernateRelationshipFactory(entityManager);
                    List<ArtifactBuilder> artifactBuilders = ExtensionFactory.createArtifactBuilders(
                            srampArtifact, content);

                    // First, need to run the artifact builders to both set metadata on srampArtifact, as well as create
                    // the derived artifacts
                    List<BaseArtifactType> derivedSrampArtifacts = new ArrayList<>();
                    for (ArtifactBuilder artifactBuilder : artifactBuilders) {
                        artifactBuilder.buildArtifacts(srampArtifact, content);
                        derivedSrampArtifacts.addAll(artifactBuilder.getDerivedArtifacts());
                    }

                    // S-RAMP -> Hibernate
                    ArtificerArtifact artificerArtifact = SrampToHibernateEntityVisitor.visit(
                                    srampArtifact, artifactType, classificationHelper);

                    if (artifactType.isDocument()) {
                        ArtificerDocumentArtifact artificerDocumentArtifact = (ArtificerDocumentArtifact) artificerArtifact;
                        processDocument(artificerDocumentArtifact, content);
                        if (content != null) {
                            // sets info on the artifact, so call prior to persisting
                            FileManagerFactory.getInstance().write(artificerDocumentArtifact, content, entityManager);
                        }
                    }

                    // persist and track the primary
                    entityManager.persist(artificerArtifact);
                    relationshipFactory.trackEntity(artificerArtifact.getUuid(), artificerArtifact);

                    Map<BaseArtifactType, ArtificerArtifact> artificerDerivedArtifacts = new HashMap<>();
                    for (BaseArtifactType derivedSrampArtifact : derivedSrampArtifacts) {
                        // persist and track each derived
                        ArtifactType derivedSrampArtifactType = ArtifactType.valueOf(derivedSrampArtifact);
                        ArtificerArtifact artificerDerivedArtifact = SrampToHibernateEntityVisitor.visit(
                                derivedSrampArtifact, derivedSrampArtifactType, classificationHelper);

                        // Handle derivation here, rather than in the relationship visitor, in case it's an *extended*
                        // derived artifact (ie, no 'relatedDocument' field).
                        artificerDerivedArtifact.setDerived(true);
                        artificerDerivedArtifact.setDerivedFrom(artificerArtifact);
                        artificerArtifact.getDerivedArtifacts().add(artificerDerivedArtifact);

                        artificerDerivedArtifacts.put(derivedSrampArtifact, artificerDerivedArtifact);
                        entityManager.persist(artificerDerivedArtifact);
                        relationshipFactory.trackEntity(artificerDerivedArtifact.getUuid(), artificerDerivedArtifact);
                    }

                    // build the relationships
                    RelationshipContext relationshipContext = new HibernateRelationshipContext();
                    for (ArtifactBuilder artifactBuilder : artifactBuilders) {
                        artifactBuilder.buildRelationships(relationshipContext);
                    }

                    // S-RAMP relationships -> Hibernate relationships
                    SrampToHibernateEntityRelationshipsVisitor.visit(srampArtifact, artificerArtifact, relationshipFactory);
                    for (BaseArtifactType derivedSrampArtifact : artificerDerivedArtifacts.keySet()) {
                        ArtificerArtifact artificerDerivedArtifact = artificerDerivedArtifacts.get(derivedSrampArtifact);
                        SrampToHibernateEntityRelationshipsVisitor.visit(derivedSrampArtifact, artificerDerivedArtifact,
                                relationshipFactory);
                    }

                    // auditing
                    if (ArtificerConfig.isAuditingEnabled()) {
                        entityManager.persist(HibernateAuditor.createAddEntry(artificerArtifact));
                        for (ArtificerArtifact derivedArtifact : artificerDerivedArtifacts.values()) {
                            entityManager.persist(HibernateAuditor.createAddEntry(derivedArtifact));
                        }
                    }

                    return artificerArtifact;
                }
            }.execute();

            return HibernateEntityToSrampVisitor.visit(artificerArtifact, artifactType, true);
        } catch (ArtificerException ae) {
            throw ae;
        } catch (Throwable t) {
            throw new ArtificerServerException(t);
        }
    }

    @Override
    public BaseArtifactType getArtifact(final String uuid, final ArtifactType artifactType) throws ArtificerException {
        return new HibernateUtil.HibernateTask<BaseArtifactType>() {
            @Override
            protected BaseArtifactType doExecute(EntityManager entityManager) throws Exception {
                try {
                    ArtificerArtifact artifact = HibernateUtil.getArtifact(uuid, entityManager, true);
                    return HibernateEntityToSrampVisitor.visit(artifact, artifactType, true);
                } catch (ArtificerNotFoundException e) {
                    return null;
                }
            }
        }.execute();
    }

    @Override
    public InputStream getArtifactContent(final String uuid, ArtifactType artifactType) throws ArtificerException {
        return new HibernateUtil.HibernateTask<InputStream>() {
            @Override
            protected InputStream doExecute(EntityManager entityManager) throws Exception {
                ArtificerDocumentArtifact artifact = (ArtificerDocumentArtifact) HibernateUtil.getArtifact(
                        uuid, entityManager, false);
                return FileManagerFactory.getInstance().read(artifact);
            }
        }.execute();
    }

    @Override
    public BaseArtifactType updateArtifact(final BaseArtifactType srampArtifact, final ArtifactType artifactType)
            throws ArtificerException {
        ArtificerArtifact artificerArtifact = new HibernateUtil.HibernateTask<ArtificerArtifact>() {
            @Override
            protected ArtificerArtifact doExecute(EntityManager entityManager) throws Exception {
                ArtificerArtifact artificerArtifact = HibernateUtil.getArtifact(srampArtifact.getUuid(), entityManager, true);

                HibernateAuditor differ = null;
                if (ArtificerConfig.isAuditingEnabled()) {
                    differ = new HibernateAuditor(artificerArtifact);
                }

                SrampToHibernateEntityVisitor.visit(srampArtifact, artificerArtifact, artifactType, classificationHelper);
                HibernateRelationshipFactory relationshipFactory = new HibernateRelationshipFactory(entityManager);
                SrampToHibernateEntityRelationshipsVisitor.visit(srampArtifact, artificerArtifact, relationshipFactory);

                if (ArtificerConfig.isAuditingEnabled()) {
                    ArtificerAuditEntry auditEntry = differ.diff(artificerArtifact);
                    entityManager.persist(auditEntry);
                }

                return artificerArtifact;
            }
        }.execute();

        try {
            return HibernateEntityToSrampVisitor.visit(artificerArtifact, artifactType, true);
        } catch (ArtificerException ae) {
            throw ae;
        } catch (Throwable t) {
            throw new ArtificerServerException(t);
        }
    }

    @Override
    public BaseArtifactType updateArtifactContent(final String uuid, final ArtifactType artifactType,
            final ArtifactContent content) throws ArtificerException {
        // This method will be removed!
        return null;
    }

    @Override
    public BaseArtifactType addComment(final String uuid, final ArtifactType artifactType, final String text)
            throws ArtificerException {
        return new HibernateUtil.HibernateTask<BaseArtifactType>() {
            @Override
            protected BaseArtifactType doExecute(EntityManager entityManager) throws Exception {
                ArtificerArtifact artifact = HibernateUtil.getArtifact(uuid, entityManager, false);

                ArtificerComment comment = new ArtificerComment();
                comment.setCreatedBy(HibernateEntityFactory.user());
                comment.setText(text);
                comment.setArtifact(artifact);
                entityManager.persist(comment);

                return HibernateEntityToSrampVisitor.visit(artifact, artifactType, true);
            }
        }.execute();
    }

    @Override
    public BaseArtifactType deleteArtifact(final String uuid, final ArtifactType artifactType, final boolean force)
            throws ArtificerException {
        return new HibernateUtil.HibernateTask<BaseArtifactType>() {
            @Override
            protected BaseArtifactType doExecute(EntityManager entityManager) throws Exception {
                ArtificerArtifact artifact = HibernateUtil.getArtifact(uuid, entityManager, false);

                List<Long> targetedArtifacts = new ArrayList<>();
                targetedArtifacts.add(artifact.getId());
                for (ArtificerArtifact derivedArtifact : artifact.getDerivedArtifacts()) {
                    targetedArtifacts.add(derivedArtifact.getId());
                }

                if (force) {
                    // delete all relationships targeting this artifact or its derived artifacts
                    Query query = entityManager.createQuery(
                            "SELECT r FROM ArtificerRelationship r INNER JOIN r.targets ts INNER JOIN ts.target t WHERE t.id IN :targetedArtifacts");
                    query.setParameter("targetedArtifacts", targetedArtifacts);
                    List<ArtificerRelationship> relationships = query.getResultList();
                    for (ArtificerRelationship relationship : relationships) {
                        entityManager.remove(relationship);
                    }
                } else {
                    // if any non-trashed generic/modeled relationships target this artifact or its derived artifacts, exception
                    Query query = entityManager.createQuery(
                            "SELECT r FROM ArtificerRelationship r INNER JOIN r.owner o INNER JOIN r.targets ts INNER JOIN ts.target t WHERE o.trashed = false AND t.id IN :targetedArtifacts AND (r.type=:type1 OR r.type=:type2)");
                    query.setParameter("targetedArtifacts", targetedArtifacts);
                    query.setParameter("type1", ArtificerRelationshipType.GENERIC);
                    query.setParameter("type2", ArtificerRelationshipType.MODELED);
                    if (query.getResultList().size() > 0) {
                        throw ArtificerConflictException.relationshipConstraint(uuid);
                    }
                }

                artifact.setTrashed(true);
                if (ArtificerConfig.isAuditingEnabled()) {
                    HibernateAuditor.createDeleteEntry(artifact);
                }
                for (ArtificerArtifact derivedArtifact : artifact.getDerivedArtifacts()) {
                    derivedArtifact.setTrashed(true);
                    if (ArtificerConfig.isAuditingEnabled()) {
                        HibernateAuditor.createDeleteEntry(derivedArtifact);
                    }
                }

                return HibernateEntityToSrampVisitor.visit(artifact, artifactType, true);
            }
        }.execute();
    }

    @Override
    public BaseArtifactType deleteArtifactContent(final String uuid, final ArtifactType artifactType)
            throws ArtificerException {
        // This method will be removed!
        return null;
    }

    @Override
    public ArtificerOntology persistOntology(final ArtificerOntology ontology) throws ArtificerException {
        if (StringUtils.isBlank(ontology.getUuid())) {
            ontology.setUuid(UUID.randomUUID().toString());
        }

        new HibernateUtil.HibernateTask<Void>() {
            @Override
            protected Void doExecute(EntityManager entityManager) throws Exception {
                // Don't trust users to properly set both sides of the association...
                for (ArtificerOntologyClass rootClass : ontology.getRootClasses()) {
                    if (rootClass.getRoot() == null) {
                        rootClass.setRoot(ontology);
                    }
                }

                entityManager.persist(ontology);
                return null;
            }
        }.execute();

        return ontology;
    }

    @Override
    public ArtificerOntology getOntology(final String uuid) throws ArtificerException {
        return new HibernateUtil.HibernateTask<ArtificerOntology>() {
            @Override
            protected ArtificerOntology doExecute(EntityManager entityManager) throws Exception {
                ArtificerOntology ontology = (ArtificerOntology) entityManager.find(ArtificerOntology.class, uuid);
                if (ontology == null) {
                    throw ArtificerNotFoundException.ontologyNotFound(uuid);
                }
                Hibernate.initialize(ontology.getRootClasses());
                return ontology;
            }
        }.execute();
    }

    @Override
    public List<ArtificerOntology> getOntologies() throws ArtificerException {
        return new HibernateUtil.HibernateTask<List<ArtificerOntology>>() {
            @Override
            protected List<ArtificerOntology> doExecute(EntityManager entityManager) throws Exception {
                Query q = entityManager.createQuery("SELECT DISTINCT o FROM ArtificerOntology o LEFT JOIN FETCH o.rootClasses ORDER BY o.label ASC");
                return q.getResultList();
            }
        }.execute();
    }

    @Override
    public void updateOntology(final ArtificerOntology ontology) throws ArtificerException {
        new HibernateUtil.HibernateTask<Void>() {
            @Override
            protected Void doExecute(EntityManager entityManager) throws Exception {
                // Don't trust users to properly set both sides of the association...
                for (ArtificerOntologyClass rootClass : ontology.getRootClasses()) {
                    if (rootClass.getRoot() == null) {
                        rootClass.setRoot(ontology);
                    }
                }
                entityManager.merge(ontology);
                return null;
            }
        }.execute();
    }

    @Override
    public void deleteOntology(final String uuid) throws ArtificerException {
        new HibernateUtil.HibernateTask<Void>() {
            @Override
            protected Void doExecute(EntityManager entityManager) throws Exception {
                // Orphan removal is not honored by JPQL, so we need to manually delete using #remove.
                ArtificerOntology ontology = entityManager.find(ArtificerOntology.class, uuid);
                if (ontology == null) {
                    throw ArtificerNotFoundException.ontologyNotFound(uuid);
                }

                entityManager.remove(ontology);

                return null;
            }
        }.execute();
    }

    @Override
    public StoredQuery persistStoredQuery(final StoredQuery srampStoredQuery) throws ArtificerException {
        // Validate the name
        if (StringUtils.isBlank(srampStoredQuery.getQueryName())) {
            throw ArtificerConflictException.storedQueryConflict();
        }

        // Check if a stored query with the given name already exists.
        try {
            getStoredQuery(srampStoredQuery.getQueryName());
            throw ArtificerConflictException.storedQueryConflict(srampStoredQuery.getQueryName());
        } catch (ArtificerNotFoundException e) {
            // do nothing -- success
        }

        new HibernateUtil.HibernateTask<Void>() {
            @Override
            protected Void doExecute(EntityManager entityManager) throws Exception {
                ArtificerStoredQuery artificerStoredQuery = HibernateEntityFactory.storedQuery(srampStoredQuery);
                entityManager.persist(artificerStoredQuery);
                return null;
            }
        }.execute();
        return srampStoredQuery;
    }

    @Override
    public void updateStoredQuery(final String queryName, final StoredQuery srampStoredQuery) throws ArtificerException {
        new HibernateUtil.HibernateTask<Void>() {
            @Override
            protected Void doExecute(EntityManager entityManager) throws Exception {
                // The name may have changed, so we need to look it up and modify, rather than just persist
                // what we're handed.
                ArtificerStoredQuery artificerStoredQuery = HibernateUtil.getStoredQuery(queryName, entityManager);
                HibernateEntityFactory.processStoredQuery(artificerStoredQuery, srampStoredQuery);
                entityManager.merge(artificerStoredQuery);
                return null;
            }
        }.execute();
    }

    @Override
    public StoredQuery getStoredQuery(final String queryName) throws ArtificerException {
        return new HibernateUtil.HibernateTask<StoredQuery>() {
            @Override
            protected StoredQuery doExecute(EntityManager entityManager) throws Exception {
                ArtificerStoredQuery storedQuery = HibernateUtil.getStoredQuery(queryName, entityManager);
                return HibernateEntityFactory.storedQuery(storedQuery);
            }
        }.execute();
    }

    @Override
    public List<StoredQuery> getStoredQueries() throws ArtificerException {
        return new HibernateUtil.HibernateTask<List<StoredQuery>>() {
            @Override
            protected List<StoredQuery> doExecute(EntityManager entityManager) throws Exception {
                List<ArtificerStoredQuery> storedQueries
                        = entityManager.createQuery("FROM ArtificerStoredQuery asq ORDER BY asq.queryName ASC").getResultList();
                return HibernateEntityFactory.storedQueries(storedQueries);
            }
        }.execute();
    }

    @Override
    public void deleteStoredQuery(final String queryName) throws ArtificerException {
        new HibernateUtil.HibernateTask<Void>() {
            @Override
            protected Void doExecute(EntityManager entityManager) throws Exception {
                // Orphan removal is not honored by JPQL, so we need to manually delete using #remove.
                ArtificerStoredQuery storedQuery = entityManager.find(ArtificerStoredQuery.class, queryName);
                if (storedQuery == null) {
                    throw ArtificerNotFoundException.storedQueryNotFound(queryName);
                }

                entityManager.remove(storedQuery);

                return null;
            }
        }.execute();
    }

    @Override
    public void printArtifactGraph(String uuid, ArtifactType type) {

    }

    @Override
    public void startup() {
        // nothing to do
    }

    @Override
    public void shutdown() {
        // nothing to do
    }

    private void processDocument(ArtificerDocumentArtifact artificerArtifact, ArtifactContent content) throws Exception {
        InputStream inputStream = null;
        try {
            if (content != null) {
                artificerArtifact.setContentSize(content.getSize());
                inputStream = content.getInputStream();
                String sha1Hash = DigestUtils.shaHex(inputStream);
                artificerArtifact.setContentHash(sha1Hash);
            } else {
                artificerArtifact.setContentSize(0);
                artificerArtifact.setContentHash("");
            }
        } finally {
            if (inputStream != null) {
                IOUtils.closeQuietly(inputStream);
            }
        }
    }
}
