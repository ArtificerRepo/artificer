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
import java.util.UUID;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DocumentArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XmlDocument;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.common.SrampException;
import org.overlord.sramp.common.SrampModelUtils;
import org.overlord.sramp.common.SrampServerException;
import org.overlord.sramp.common.derived.LinkerContext;
import org.overlord.sramp.common.visitors.ArtifactVisitorHelper;
import org.overlord.sramp.repository.DerivedArtifactsFactory;
import org.overlord.sramp.repository.jcr.audit.JCRAuditConstants;
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
                new JCRReferenceFactoryImpl(session), classificationHelper);
        ArtifactVisitorHelper.visitArtifact(visitor, metaData);
        if (visitor.hasError())
            throw visitor.getError();

        log.debug("Successfully saved {} to node={}", name, uuid);
        session.getWorkspace().getObservationManager().setUserData(JCRAuditConstants.AUDIT_BUNDLE_ARTIFACT_ADDED_PHASE1);
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
        session.getWorkspace().getObservationManager().setUserData(JCRAuditConstants.AUDIT_BUNDLE_ARTIFACT_ADDED_PHASE2);
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
        session.getWorkspace().getObservationManager().setUserData(JCRAuditConstants.AUDIT_BUNDLE_ARTIFACT_ADDED_PHASE3);
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
                        derivedArtifactNode, null, classificationHelper);
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

                log.debug("Successfully saved derived artifact {}'s relationships.", derivedArtifact.getName());
            }

            // Persist phase 2 (the relationships)
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
     * Saves binary content from the given JCR content (jcr:content) node to a temporary
     * file.
     * @param jcrContentNode
     * @param tempFile
     * @throws Exception
     */
    public static File saveToTempFile(Node jcrContentNode) throws Exception {
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

    public static class Phase1Result {
        public ArtifactType artifactType;
        public Node artifactNode;
        public boolean isDocumentArtifact;
    }

    public static class Phase2Result {
        public Collection<BaseArtifactType> derivedArtifacts;
    }
}
