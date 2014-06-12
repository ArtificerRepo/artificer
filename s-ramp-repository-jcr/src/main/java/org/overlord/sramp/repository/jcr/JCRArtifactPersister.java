/*
 * Copyright 2013 JBoss Inc
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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.UUID;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DocumentArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XmlDocument;
import org.overlord.sramp.common.ArtifactAlreadyExistsException;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.common.Sramp;
import org.overlord.sramp.common.SrampException;
import org.overlord.sramp.common.SrampModelUtils;
import org.overlord.sramp.common.SrampServerException;
import org.overlord.sramp.common.audit.AuditEntryTypes;
import org.overlord.sramp.common.audit.AuditItemTypes;
import org.overlord.sramp.common.derived.LinkerContext;
import org.overlord.sramp.common.visitors.ArtifactVisitorHelper;
import org.overlord.sramp.repository.DerivedArtifactsFactory;
import org.overlord.sramp.repository.jcr.audit.ArtifactDiff;
import org.overlord.sramp.repository.jcr.audit.ArtifactJCRNodeDiffer;
import org.overlord.sramp.repository.jcr.i18n.Messages;
import org.overlord.sramp.repository.jcr.mapper.ArtifactToJCRNodeVisitor;
import org.overlord.sramp.repository.jcr.util.JCRUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class - breaks up the work of persisting an artifact into composable chunks.
 *
 * @author eric.wittmann@redhat.com
 */
public final class JCRArtifactPersister {

    private static Logger log = LoggerFactory.getLogger(JCRArtifactPersister.class);
    private static Sramp sramp = new Sramp();

    /**
     * Phase one of persisting an artifact consists of creating the JCR node for the artifact and
     * persisting all of its meta-data to it.
     * @param session
     * @param metaData
     * @param content
     * @param referenceFactory
     * @param classificationHelper
     * @throws Exception
     */
    public static Phase1Result persistArtifactPhase1(Session session, BaseArtifactType metaData,
            InputStream content, ClassificationHelper classificationHelper) throws Exception {
        JCRUtils tools = new JCRUtils();
        if (metaData.getUuid() == null) {
            metaData.setUuid(UUID.randomUUID().toString());
        }
        String uuid = metaData.getUuid();
        ArtifactType artifactType = ArtifactType.valueOf(metaData);
        String name = metaData.getName();
        String artifactPath = MapToJCRPath.getArtifactPath(uuid);
        if (session.nodeExists(artifactPath)) {
            throw new ArtifactAlreadyExistsException(uuid);
        }
        log.debug(Messages.i18n.format("UPLOADING_TO_JCR", name)); //$NON-NLS-1$

        Node artifactNode = null;
        boolean isDocumentArtifact = SrampModelUtils.isDocumentArtifact(metaData);
        if (content == null && !isDocumentArtifact) {
            artifactNode = tools.findOrCreateNode(session, artifactPath, "nt:folder", JCRConstants.SRAMP_NON_DOCUMENT_TYPE); //$NON-NLS-1$
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
            artifactNode.setProperty(JCRConstants.SRAMP_EXTENDED_TYPE, artifactType.getExtendedType());
        }
        // Extended Document
        if (ExtendedDocument.class.isAssignableFrom(artifactType.getArtifactType().getTypeClass())) {
            artifactNode.setProperty(JCRConstants.SRAMP_EXTENDED_TYPE, artifactType.getExtendedType());
        }
        // Document
        if (DocumentArtifactType.class.isAssignableFrom(artifactType.getArtifactType().getTypeClass())) {
            artifactNode.setProperty(JCRConstants.SRAMP_CONTENT_TYPE, artifactType.getMimeType());
            artifactNode.setProperty(JCRConstants.SRAMP_CONTENT_SIZE, artifactNode.getProperty("jcr:content/jcr:data").getLength()); //$NON-NLS-1$
            String sha1Hash = JCRExtensions.getInstance().getSha1Hash(
                    artifactNode.getProperty("jcr:content/jcr:data").getBinary()); //$NON-NLS-1$
            artifactNode.setProperty(JCRConstants.SRAMP_CONTENT_HASH, sha1Hash);
        }
        // XMLDocument
        if (XmlDocument.class.isAssignableFrom(artifactType.getArtifactType().getTypeClass())) {
            // read the encoding from the header
            artifactNode.setProperty(JCRConstants.SRAMP_CONTENT_ENCODING, "UTF-8"); //$NON-NLS-1$
        }

        // Update the JCR node with any properties included in the meta-data
        ArtifactToJCRNodeVisitor visitor = new ArtifactToJCRNodeVisitor(artifactType, artifactNode,
                new JCRReferenceFactoryImpl(session), classificationHelper);
        ArtifactVisitorHelper.visitArtifact(visitor, metaData);
        if (visitor.hasError())
            throw visitor.getError();

        log.debug(Messages.i18n.format("SAVED_JCR_NODE", name, uuid)); //$NON-NLS-1$
        if (sramp.isAuditingEnabled()) {
            auditCreateArtifact(artifactNode);
            session.save();
        }
        session.save();

        Phase1Result result = new Phase1Result();
        result.artifactNode = artifactNode;
        result.artifactType = artifactType;
        result.isDocumentArtifact = isDocumentArtifact;
        return result;
    }

    /**
     * Phase two of artifact persistence consists of creating derived content for the artifact
     * and creating the JCR nodes associated with the derived content.  No relationships are
     * created in this phase.
     *
     * @param session
     * @param metaData
     * @param classificationHelper
     * @param phase1
     * @throws Exception
     */
    public static Phase2Result persistArtifactPhase2(Session session, BaseArtifactType metaData,
            ClassificationHelper classificationHelper, Phase1Result phase1) throws Exception {
        // No need to do any of the artifact deriving in phase2 unless it's a derivable (document
        // style) artifact
        if (!phase1.isDocumentArtifact)
            return null;
        Node artifactNode = phase1.artifactNode;
        ArtifactType artifactType = phase1.artifactType;

        Collection<BaseArtifactType> derivedArtifacts = null;
        InputStream cis = null;
        File tempFile = null;
        try {
            Node artifactContentNode = artifactNode.getNode("jcr:content"); //$NON-NLS-1$
            tempFile = saveToTempFile(artifactContentNode);
            cis = FileUtils.openInputStream(tempFile);
            derivedArtifacts = DerivedArtifactsFactory.newInstance().deriveArtifacts(metaData, cis);
        } finally {
            IOUtils.closeQuietly(cis);
            FileUtils.deleteQuietly(tempFile);
        }

        // Persist any derived artifacts.
        if (derivedArtifacts != null) {
            persistDerivedArtifacts(session, artifactNode, derivedArtifacts, classificationHelper);
        }

        // Update the JCR node again, this time with any properties/relationships added to the meta-data
        // by the deriver
        ArtifactToJCRNodeVisitor visitor = new ArtifactToJCRNodeVisitor(artifactType, artifactNode,
                new JCRReferenceFactoryImpl(session), classificationHelper);
        ArtifactVisitorHelper.visitArtifact(visitor, metaData);
        if (visitor.hasError())
            throw visitor.getError();

        // JCR persist point - phase 2 of artifact create
        session.save();

        Phase2Result result = new Phase2Result();
        result.derivedArtifacts = derivedArtifacts;
        return result;
    }

    /**
     * Phase 3 of artifact persistence consists of linking all derived artifacts.  The linkage phase
     * gives derivers the opportunity to create relationships between the derived artifacts and other
     * artifacts found in the repository.
     * @param session
     * @param metaData
     * @param classificationHelper
     * @param phase1
     * @param phase2
     * @throws Exception
     */
    public static void persistArtifactPhase3(Session session, BaseArtifactType metaData,
            ClassificationHelper classificationHelper, Phase1Result phase1, Phase2Result phase2) throws Exception {
        // No need to do any of the artifact deriving in phase2 unless it's a derivable (document
        // style) artifact
        if (!phase1.isDocumentArtifact)
            return;

        Collection<BaseArtifactType> derivedArtifacts = phase2.derivedArtifacts;
        Node artifactNode = phase1.artifactNode;

        // Now execute the derived artifact linker phase, creating relationships between the various
        // artifacts derived above.
        if (derivedArtifacts != null && !derivedArtifacts.isEmpty()) {
            LinkerContext context = new JCRLinkerContext(session);
            DerivedArtifactsFactory.newInstance().linkArtifacts(context, metaData, derivedArtifacts);
            persistDerivedArtifactsRelationships(session, artifactNode, derivedArtifacts, classificationHelper);
        }

        // JCR persist point - phase 3 of artifact create (only for document style artifacts
        // with derived content)
        session.save();
    }

    /**
     * Persist any derived artifacts to JCR.
     * @param session
     * @param sourceArtifactNode
     * @param derivedArtifacts
     * @param classificationHelper
     * @throws SrampException
     */
    private static void persistDerivedArtifacts(Session session, Node sourceArtifactNode,
            Collection<BaseArtifactType> derivedArtifacts, ClassificationHelper classificationHelper)
            throws SrampException {
        try {
            // Persist each of the derived nodes
            for (BaseArtifactType derivedArtifact : derivedArtifacts) {
                if (derivedArtifact.getUuid() == null) {
                    throw new SrampServerException(Messages.i18n.format("MISSING_DERIVED_UUID", derivedArtifact.getName())); //$NON-NLS-1$
                }
                ArtifactType derivedArtifactType = ArtifactType.valueOf(derivedArtifact);
                String jcrMixinName = derivedArtifactType.getArtifactType().getApiType().value();
                if (derivedArtifactType.isExtendedType()) {
                    jcrMixinName = "extendedDerivedArtifactType"; //$NON-NLS-1$
                    derivedArtifactType.setExtendedDerivedType(true);
                }
                jcrMixinName = JCRConstants.SRAMP_ + StringUtils.uncapitalize(jcrMixinName);

                // Create the JCR node and set some basic properties first.
                String nodeName = derivedArtifact.getUuid();
                Node derivedArtifactNode = sourceArtifactNode.addNode(nodeName, JCRConstants.SRAMP_DERIVED_PRIMARY_TYPE);
                derivedArtifactNode.addMixin(jcrMixinName);
                derivedArtifactNode.setProperty(JCRConstants.SRAMP_UUID, derivedArtifact.getUuid());
                derivedArtifactNode.setProperty(JCRConstants.SRAMP_ARTIFACT_MODEL, derivedArtifactType.getArtifactType().getModel());
                derivedArtifactNode.setProperty(JCRConstants.SRAMP_ARTIFACT_TYPE, derivedArtifactType.getArtifactType().getType());
                // Extended
                if (ExtendedArtifactType.class.isAssignableFrom(derivedArtifactType.getArtifactType().getTypeClass())) {
                    // read the encoding from the header
                    derivedArtifactNode.setProperty(JCRConstants.SRAMP_EXTENDED_TYPE, derivedArtifactType.getExtendedType());
                }

                // It's definitely derived.
                derivedArtifactNode.setProperty("sramp:derived", true); //$NON-NLS-1$

                // Create the visitor that will be used to write the artifact information to the JCR node
                ArtifactToJCRNodeVisitor visitor = new ArtifactToJCRNodeVisitor(derivedArtifactType,
                        derivedArtifactNode, null, classificationHelper);
                visitor.setProcessRelationships(false);
                ArtifactVisitorHelper.visitArtifact(visitor, derivedArtifact);
                if (visitor.hasError())
                    throw visitor.getError();

                // Audit the create event for the derived node
                if (sramp.isAuditingEnabled() && sramp.isDerivedArtifactAuditingEnabled()) {
                    auditCreateArtifact(derivedArtifactNode);
                }

                log.debug(Messages.i18n.format("SAVED_DERIVED_ARTY_TO_JCR", derivedArtifact.getName(), derivedArtifact.getUuid())); //$NON-NLS-1$
            }

            // Save current changes so that references to nodes can be found.  Note that if
            // transactions are enabled, this will not actually persist to final storage.
            session.save();

            log.debug(Messages.i18n.format("SAVED_ARTIFACTS", derivedArtifacts.size())); //$NON-NLS-1$
        } catch (SrampException e) {
            throw e;
        } catch (Throwable t) {
            throw new SrampServerException(t);
        }
    }

    /**
     * Perists the derived artifacts again due to
     * @param session
     * @param sourceArtifactNode
     * @param derivedArtifacts
     * @param classificationHelper
     * @throws SrampException
     */
    private static void persistDerivedArtifactsRelationships(Session session, Node sourceArtifactNode,
            Collection<BaseArtifactType> derivedArtifacts, ClassificationHelper classificationHelper) throws SrampException {
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
                        derivedArtifactNode, referenceFactory, classificationHelper);
                visitor.setProcessRelationships(true);
                ArtifactVisitorHelper.visitArtifact(visitor, derivedArtifact);
                if (visitor.hasError())
                    throw visitor.getError();

                log.debug(Messages.i18n.format("SAVED_RELATIONSHIPS", derivedArtifact.getName())); //$NON-NLS-1$
            }

            // Persist phase 2 (the relationships)
            session.save();

            log.debug(Messages.i18n.format("SAVED_ARTIFACTS_2", derivedArtifacts.size())); //$NON-NLS-1$
        } catch (SrampException e) {
            throw e;
        } catch (Throwable t) {
            throw new SrampServerException(t);
        }
    }

    /**
     * Saves binary content from the given JCR content (jcr:content) node to a temporary
     * file.
     * @param jcrContentNode
     * @param tempFile
     * @throws Exception
     */
    public static File saveToTempFile(Node jcrContentNode) throws Exception {
        File file = File.createTempFile("sramp", ".jcr"); //$NON-NLS-1$ //$NON-NLS-2$
        Binary binary = null;
        InputStream content = null;
        OutputStream tempFileOS = null;

        try {
            binary = jcrContentNode.getProperty("jcr:data").getBinary(); //$NON-NLS-1$
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

    public static class Phase1Result {
        public ArtifactType artifactType;
        public Node artifactNode;
        public boolean isDocumentArtifact;
    }

    public static class Phase2Result {
        public Collection<BaseArtifactType> derivedArtifacts;
    }


    /**
     * Audits an artifact create event.  This will add an audit entry as a child of the
     * new artifact JCR node of type "artifact:add".  In addition, the initial state of
     * all properties, classifiers, and relationships will be recorded.
     * @param artifactNode
     * @throws RepositoryException
     */
    public static void auditCreateArtifact(Node artifactNode) throws RepositoryException {
        ArtifactJCRNodeDiffer differ = new ArtifactJCRNodeDiffer(artifactNode);
        Node auditEntryNode = createAuditEntryNode(artifactNode, AuditEntryTypes.ARTIFACT_ADD);
        Node propAddedNode = createAuditItemNode(auditEntryNode, AuditItemTypes.PROPERTY_ADDED);
        for (Entry<String, String> entry : differ.getProperties().entrySet()) {
            propAddedNode.setProperty(entry.getKey(), entry.getValue());
        }
        if (!differ.getClassifiers().isEmpty()) {
            Node classifierAddedNode = createAuditItemNode(auditEntryNode, AuditItemTypes.CLASSIFIERS_ADDED);
            int idx = 0;
            for (String classifier : differ.getClassifiers()) {
                classifierAddedNode.setProperty("classifier-" + idx++, classifier); //$NON-NLS-1$
            }
        }
    }

    /**
     * Audits an artifact update event.  This will add an audit entry as a child of the
     * new artifact JCR node of type "artifact:update".  In addition, any changes to
     * properties, classifiers, or relationships will be added as audit items to the
     * audit entry.
     * @param artifactNode
     * @throws RepositoryException
     */
    public static void auditUpdateArtifact(ArtifactJCRNodeDiffer differ, Node artifactNode) throws RepositoryException {
        Node auditEntryNode = createAuditEntryNode(artifactNode, AuditEntryTypes.ARTIFACT_UPDATE);

        ArtifactDiff diff = differ.diff(artifactNode);
        if (!diff.getAddedProperties().isEmpty()) {
            Node propAddedNode = createAuditItemNode(auditEntryNode, AuditItemTypes.PROPERTY_ADDED);
            for (Entry<String, String> entry : diff.getAddedProperties().entrySet()) {
                propAddedNode.setProperty(entry.getKey(), entry.getValue());
            }
        }
        if (!diff.getUpdatedProperties().isEmpty()) {
            Node propChangedNode = createAuditItemNode(auditEntryNode, AuditItemTypes.PROPERTY_CHANGED);
            for (Entry<String, String> entry : diff.getUpdatedProperties().entrySet()) {
                propChangedNode.setProperty(entry.getKey(), entry.getValue());
            }
        }
        if (!diff.getDeletedProperties().isEmpty()) {
            Node propRemovedNode = createAuditItemNode(auditEntryNode, AuditItemTypes.PROPERTY_REMOVED);
            for (String propName : diff.getDeletedProperties()) {
                propRemovedNode.setProperty(propName, ""); //$NON-NLS-1$
            }
        }
        if (!diff.getAddedClassifiers().isEmpty()) {
            Node classifiersAddedNode = createAuditItemNode(auditEntryNode, AuditItemTypes.CLASSIFIERS_ADDED);
            int idx = 0;
            for (String classifier : diff.getAddedClassifiers()) {
                classifiersAddedNode.setProperty("classifier-" + idx++, classifier); //$NON-NLS-1$
            }
        }
        if (!diff.getDeletedClassifiers().isEmpty()) {
            Node classifiersRemovedNode = createAuditItemNode(auditEntryNode, AuditItemTypes.CLASSIFIERS_REMOVED);
            int idx = 0;
            for (String classifier : diff.getDeletedClassifiers()) {
                classifiersRemovedNode.setProperty("classifier-" + idx++, classifier); //$NON-NLS-1$
            }
        }
    }

    /**
     * Creates a JCR node for a single audit entry for a single artifact.  This is called when
     * recording audit information for an artifact.
     * @param artifactNode
     * @param when
     * @throws ValueFormatException
     * @throws VersionException
     * @throws LockException
     * @throws ConstraintViolationException
     * @throws RepositoryException
     */
    public static Node createAuditEntryNode(Node artifactNode, String type) throws ValueFormatException,
            VersionException, LockException, ConstraintViolationException, RepositoryException {
        String auditUuid = UUID.randomUUID().toString();
        Node auditEntryNode = artifactNode.addNode("audit:" + auditUuid, JCRConstants.SRAMP_AUDIT_ENTRY); //$NON-NLS-1$

        auditEntryNode.setProperty("audit:uuid", auditUuid); //$NON-NLS-1$
        auditEntryNode.setProperty("audit:sortId", System.currentTimeMillis()); //$NON-NLS-1$
        auditEntryNode.setProperty("audit:type", type); //$NON-NLS-1$

        return auditEntryNode;
    }

    /**
     * Creates the audit item node as a child of the given audit entry.
     * @param auditEntryNode
     * @param propertyAdded
     * @throws RepositoryException
     * @throws ConstraintViolationException
     * @throws LockException
     * @throws VersionException
     * @throws ValueFormatException
     */
    public static Node createAuditItemNode(Node auditEntryNode, String auditItemType)
            throws ValueFormatException, VersionException, LockException, ConstraintViolationException,
            RepositoryException {
        String auditItemNodeName = "audit:" + auditItemType.replace(':', '_'); //$NON-NLS-1$
        Node auditItemNode = auditEntryNode.addNode(auditItemNodeName, JCRConstants.SRAMP_AUDIT_ITEM);
        auditItemNode.setProperty("audit:type", auditItemType); //$NON-NLS-1$
        return auditItemNode;
    }

}
