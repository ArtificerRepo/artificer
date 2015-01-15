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

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.*;
import org.overlord.sramp.common.*;
import org.overlord.sramp.integration.artifactbuilder.ArtifactBuilder;
import org.overlord.sramp.integration.artifactbuilder.RelationshipContext;
import org.overlord.sramp.common.audit.AuditEntryTypes;
import org.overlord.sramp.common.audit.AuditItemTypes;
import org.overlord.sramp.repository.error.ArtifactConflictException;
import org.overlord.sramp.common.error.SrampServerException;
import org.overlord.sramp.common.visitors.ArtifactVisitorHelper;
import org.overlord.sramp.integration.ExtensionFactory;
import org.overlord.sramp.repository.jcr.audit.ArtifactJCRNodeDiff;
import org.overlord.sramp.repository.jcr.audit.ArtifactJCRNodeDiffer;
import org.overlord.sramp.repository.jcr.i18n.Messages;
import org.overlord.sramp.repository.jcr.mapper.ArtifactToJCRNodeVisitor;
import org.overlord.sramp.repository.jcr.util.JCRUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;
import java.io.*;
import java.lang.System;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

/**
 * Helper class - breaks up the work of persisting an artifact into composable phases.  The phases are mainly necessary
 * to simultaneously support single and batch actions.
 *
 * @author Brett Meyer
 * @author eric.wittmann@redhat.com
 */
public final class JCRArtifactPersister {

    private static Logger log = LoggerFactory.getLogger(JCRArtifactPersister.class);

    private final BaseArtifactType primaryArtifact;
    private final ArtifactContent artifactContent;
    private final List<ArtifactBuilder> artifactBuilders;
    private final ClassificationHelper classificationHelper;

    private Node primaryArtifactNode;
    private List<BaseArtifactType> derivedArtifacts;

    public JCRArtifactPersister(BaseArtifactType primaryArtifact, ArtifactContent artifactContent,
            ClassificationHelper classificationHelper) throws Exception {
        this.primaryArtifact = primaryArtifact;
        this.artifactContent = artifactContent;
        this.classificationHelper = classificationHelper;
        artifactBuilders = ExtensionFactory.createArtifactBuilders(primaryArtifact, artifactContent);
    }

    public void persistArtifact(Session session) throws Exception {
        if (StringUtils.isBlank(primaryArtifact.getUuid())) {
            primaryArtifact.setUuid(UUID.randomUUID().toString());
        }

        runArtifactBuilders();

        primaryArtifactNode = persistPrimaryArtifact(session);
        persistDerivedArtifacts(session);
    }

    public void updateArtifactContent(Node primaryArtifactNode, Session session) throws Exception {
        this.primaryArtifactNode = primaryArtifactNode;
        ArtifactType artifactType = ArtifactType.valueOf(primaryArtifact);

        runArtifactBuilders();

        JCRUtils.uploadFile(session, primaryArtifactNode.getPath(), artifactContent.getInputStream());
        JCRUtils.setArtifactContentMimeType(primaryArtifactNode, artifactType.getMimeType());
        persistDocumentProperties(primaryArtifactNode, artifactType);

        // Update the JCR node with any properties included in the meta-data
        ArtifactToJCRNodeVisitor visitor = new ArtifactToJCRNodeVisitor(artifactType, primaryArtifactNode,
                new JCRReferenceFactoryImpl(session), classificationHelper);
        visitor.setProcessRelationships(false);
        ArtifactVisitorHelper.visitArtifact(visitor, primaryArtifact);
        visitor.throwError();

        session.save();

        persistDerivedArtifacts(session);
    }

    public void persistArtifactRelationships(Session session) throws Exception {
        if (SrampModelUtils.isDocumentArtifact(primaryArtifact)) {
            RelationshipContext relationshipContext = new JCRRelationshipContext(session);
            for (ArtifactBuilder artifactBuilder : artifactBuilders) {
                artifactBuilder.buildRelationships(relationshipContext);
            }

            persistDerivedArtifactsRelationships(session);
        }

        persistPrimaryArtifactRelationships(session);
    }

    private void runArtifactBuilders() throws Exception {
        // NOTE: The artifact builders *must* be run *before* persisting the primary artifact, and *must* be done
        // regardless if it's a doc artifact.  Some builders are responsible for setting custom properties on the
        // primary artifact.  See JavaClassArtifactBuilder as an example.
        derivedArtifacts = new ArrayList<BaseArtifactType>();
        for (ArtifactBuilder artifactBuilder : artifactBuilders) {
            artifactBuilder.buildArtifacts(primaryArtifact, artifactContent);
            derivedArtifacts.addAll(artifactBuilder.getDerivedArtifacts());
        }
    }

    private Node persistPrimaryArtifact(Session session) throws Exception {
        String uuid = primaryArtifact.getUuid();
        ArtifactType artifactType = ArtifactType.valueOf(primaryArtifact);
        String name = primaryArtifact.getName();
        String artifactPath = MapToJCRPath.getArtifactPath(uuid);
        if (session.nodeExists(artifactPath)) {
            throw new ArtifactConflictException(uuid);
        }
        log.debug(Messages.i18n.format("UPLOADING_TO_JCR", name));

        Node artifactNode = null;
        boolean isDocumentArtifact = SrampModelUtils.isDocumentArtifact(primaryArtifact);
        if (!isDocumentArtifact) {
            artifactNode = JCRUtils.findOrCreateNode(session, artifactPath, "nt:folder", JCRConstants.SRAMP_NON_DOCUMENT_TYPE);
        } else {
            // Some versions of ModeShape do not allow 'null' Binary values, so we must give a valid IS.
            InputStream is = artifactContent == null ? new ByteArrayInputStream(new byte[0]) : artifactContent.getInputStream();
            artifactNode = JCRUtils.uploadFile(session, artifactPath, is);
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

        persistDocumentProperties(artifactNode, artifactType);

        // Update the JCR node with any properties included in the meta-data
        ArtifactToJCRNodeVisitor visitor = new ArtifactToJCRNodeVisitor(artifactType, artifactNode,
                new JCRReferenceFactoryImpl(session), classificationHelper);
        visitor.setProcessRelationships(false);
        ArtifactVisitorHelper.visitArtifact(visitor, primaryArtifact);
        visitor.throwError();

        log.debug(Messages.i18n.format("SAVED_JCR_NODE", name, uuid));
        if (SrampConfig.isAuditingEnabled()) {
            auditCreateArtifact(artifactNode);
            session.save();
        }
        session.save();

        return artifactNode;
    }

    private void persistDocumentProperties(Node artifactNode, ArtifactType artifactType) throws Exception {
        // Document
        if (DocumentArtifactType.class.isAssignableFrom(artifactType.getArtifactType().getTypeClass())) {
            artifactNode.setProperty(JCRConstants.SRAMP_CONTENT_TYPE, artifactType.getMimeType());
            artifactNode.setProperty(JCRConstants.SRAMP_CONTENT_SIZE, artifactNode.getProperty(JCRConstants.JCR_CONTENT_DATA).getLength());
            Binary binary = artifactNode.getProperty(JCRConstants.JCR_CONTENT_DATA).getBinary();
            if (binary != null) {
                String sha1Hash = JCRExtensions.getInstance().getSha1Hash(binary);
                artifactNode.setProperty(JCRConstants.SRAMP_CONTENT_HASH, sha1Hash);
            }
        }
        // XMLDocument
        if (primaryArtifact instanceof XmlDocument) {
            artifactNode.setProperty(JCRConstants.SRAMP_CONTENT_ENCODING,
                    ((XmlDocument)primaryArtifact).getContentEncoding());
        }
    }
    
    private void persistPrimaryArtifactRelationships(Session session) throws SrampException {
        try {
            // Update the JCR node again, this time with any relationships resolved by the linker
            ArtifactToJCRNodeVisitor visitor = new ArtifactToJCRNodeVisitor(ArtifactType.valueOf(primaryArtifact),
                    primaryArtifactNode, new JCRReferenceFactoryImpl(session), classificationHelper);
            ArtifactVisitorHelper.visitArtifact(visitor, primaryArtifact);
            visitor.throwError();
            
            session.save();
        } catch (SrampException e) {
            throw e;
        } catch (Throwable t) {
            throw new SrampServerException(t);
        }
    }

    private void persistDerivedArtifacts(Session session) throws SrampException {
        try {
            // Persist each of the derived nodes
            for (BaseArtifactType derivedArtifact : derivedArtifacts) {
                if (derivedArtifact.getUuid() == null) {
                    throw new SrampServerException(Messages.i18n.format("MISSING_DERIVED_UUID", derivedArtifact.getName()));
                }
                ArtifactType derivedArtifactType = ArtifactType.valueOf(derivedArtifact);
                String jcrMixinName = derivedArtifactType.getArtifactType().getApiType().value();
                if (derivedArtifactType.isExtendedType()) {
                    jcrMixinName = "extendedDerivedArtifactType";
                    derivedArtifactType.setExtendedDerivedType(true);
                }
                jcrMixinName = JCRConstants.SRAMP_ + StringUtils.uncapitalize(jcrMixinName);

                // Create the JCR node and set some basic properties first.
                String nodeName = derivedArtifact.getUuid();
                Node derivedArtifactNode = primaryArtifactNode.addNode(nodeName, JCRConstants.SRAMP_DERIVED_PRIMARY_TYPE);
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
                derivedArtifactNode.setProperty(JCRConstants.SRAMP_DERIVED, true);

                // Create the visitor that will be used to write the artifact information to the JCR node
                ArtifactToJCRNodeVisitor visitor = new ArtifactToJCRNodeVisitor(derivedArtifactType,
                        derivedArtifactNode, null, classificationHelper);
                visitor.setProcessRelationships(false);
                ArtifactVisitorHelper.visitArtifact(visitor, derivedArtifact);
                visitor.throwError();

                // Audit the create event for the derived node
                if (SrampConfig.isAuditingEnabled() && SrampConfig.isDerivedArtifactAuditingEnabled()) {
                    auditCreateArtifact(derivedArtifactNode);
                }

                log.debug(Messages.i18n.format("SAVED_DERIVED_ARTY_TO_JCR", derivedArtifact.getName(), derivedArtifact.getUuid()));
            }

            // Save current changes so that references to nodes can be found.  Note that if
            // transactions are enabled, this will not actually persist to final storage.
            session.save();
        } catch (SrampException e) {
            throw e;
        } catch (Throwable t) {
            throw new SrampServerException(t);
        }
    }

    private void persistDerivedArtifactsRelationships(Session session) throws SrampException {
        try {
            // Persist each of the derived nodes
            JCRReferenceFactoryImpl referenceFactory = new JCRReferenceFactoryImpl(session);
            for (BaseArtifactType derivedArtifact : derivedArtifacts) {
                ArtifactType derivedArtifactType = ArtifactType.valueOf(derivedArtifact);
                if (derivedArtifactType.isExtendedType()) {
                    derivedArtifactType.setExtendedDerivedType(true);
                }
                Node derivedArtifactNode = primaryArtifactNode.getNode(derivedArtifact.getUuid());

                // Create the visitor that will be used to write the artifact information to the JCR node
                ArtifactToJCRNodeVisitor visitor = new ArtifactToJCRNodeVisitor(derivedArtifactType,
                        derivedArtifactNode, referenceFactory, classificationHelper);
                ArtifactVisitorHelper.visitArtifact(visitor, derivedArtifact);
                visitor.throwError();

                log.debug(Messages.i18n.format("SAVED_RELATIONSHIPS", derivedArtifact.getName()));
            }
        } catch (SrampException e) {
            throw e;
        } catch (Throwable t) {
            throw new SrampServerException(t);
        }
    }

    public Node getPrimaryArtifactNode() {
        return primaryArtifactNode;
    }

    /**
     * Saves binary content from the given JCR content (jcr:content) node to a temporary
     * file.
     * @param jcrContentNode
     * @throws Exception
     */
    public static File saveToTempFile(Node jcrContentNode) throws Exception {
        File file = File.createTempFile("sramp", ".jcr");
        Binary binary = null;
        InputStream content = null;
        OutputStream tempFileOS = null;

        try {
            binary = jcrContentNode.getProperty(JCRConstants.JCR_DATA).getBinary();
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
                classifierAddedNode.setProperty("classifier-" + idx++, classifier);
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

        ArtifactJCRNodeDiff diff = differ.diff(artifactNode);
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
                propRemovedNode.setProperty(propName, "");
            }
        }
        if (!diff.getAddedClassifiers().isEmpty()) {
            Node classifiersAddedNode = createAuditItemNode(auditEntryNode, AuditItemTypes.CLASSIFIERS_ADDED);
            int idx = 0;
            for (String classifier : diff.getAddedClassifiers()) {
                classifiersAddedNode.setProperty("classifier-" + idx++, classifier);
            }
        }
        if (!diff.getDeletedClassifiers().isEmpty()) {
            Node classifiersRemovedNode = createAuditItemNode(auditEntryNode, AuditItemTypes.CLASSIFIERS_REMOVED);
            int idx = 0;
            for (String classifier : diff.getDeletedClassifiers()) {
                classifiersRemovedNode.setProperty("classifier-" + idx++, classifier);
            }
        }
    }

    /**
     * Creates a JCR node for a single audit entry for a single artifact.  This is called when
     * recording audit information for an artifact.
     * @param artifactNode
     * @throws ValueFormatException
     * @throws VersionException
     * @throws LockException
     * @throws ConstraintViolationException
     * @throws RepositoryException
     */
    public static Node createAuditEntryNode(Node artifactNode, String type) throws ValueFormatException,
            VersionException, LockException, ConstraintViolationException, RepositoryException {
        String auditUuid = UUID.randomUUID().toString();
        Node auditEntryNode = artifactNode.addNode("audit:" + auditUuid, JCRConstants.SRAMP_AUDIT_ENTRY);

        auditEntryNode.setProperty("audit:uuid", auditUuid);
        auditEntryNode.setProperty("audit:sortId", System.currentTimeMillis());
        auditEntryNode.setProperty("audit:type", type);

        return auditEntryNode;
    }

    /**
     * Creates the audit item node as a child of the given audit entry.
     * @param auditEntryNode
     * @throws RepositoryException
     * @throws ConstraintViolationException
     * @throws LockException
     * @throws VersionException
     * @throws ValueFormatException
     */
    public static Node createAuditItemNode(Node auditEntryNode, String auditItemType)
            throws ValueFormatException, VersionException, LockException, ConstraintViolationException,
            RepositoryException {
        String auditItemNodeName = "audit:" + auditItemType.replace(':', '_');
        Node auditItemNode = auditEntryNode.addNode(auditItemNodeName, JCRConstants.SRAMP_AUDIT_ITEM);
        auditItemNode.setProperty("audit:type", auditItemType);
        return auditItemNode;
    }

}
