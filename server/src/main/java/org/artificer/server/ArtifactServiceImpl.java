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
package org.artificer.server;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.artificer.atom.visitors.ArtifactContentTypeVisitor;
import org.artificer.common.ArtifactContent;
import org.artificer.common.ArtifactType;
import org.artificer.common.ArtifactTypeEnum;
import org.artificer.common.ArtifactVerifier;
import org.artificer.common.ArtificerConstants;
import org.artificer.common.error.ArtificerNotFoundException;
import org.artificer.common.error.ArtificerUserException;
import org.artificer.common.visitors.ArtifactVisitorHelper;
import org.artificer.events.EventProducer;
import org.artificer.events.EventProducerFactory;
import org.artificer.integration.ArchiveContext;
import org.artificer.integration.ExtensionFactory;
import org.artificer.repository.PersistenceManager;
import org.artificer.server.core.api.ArtifactService;
import org.artificer.server.i18n.Messages;
import org.artificer.server.mime.MimeTypes;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DocumentArtifactType;

import javax.ejb.Remote;
import javax.ejb.Stateful;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * @author Brett Meyer.
 */
@Stateful(name = "ArtifactService")
@Remote(ArtifactService.class)
// Required so that artificer-repository-hibernate can control the transactions during EJB calls.
@TransactionManagement(TransactionManagementType.BEAN)
public class ArtifactServiceImpl extends AbstractServiceImpl implements ArtifactService {

    @Override
    public BaseArtifactType create(BaseArtifactType artifact) throws Exception {
        ArtifactType artifactType = ArtifactType.valueOf(artifact);
        return create(artifactType, artifact);
    }

    @Override
    public BaseArtifactType create(ArtifactType artifactType, BaseArtifactType artifact) throws Exception {
        ArtifactVerifier verifier = new ArtifactVerifier(artifactType);
        ArtifactVisitorHelper.visitArtifact(verifier, artifact);
        verifier.throwError();

        if (artifactType.isDerived()) {
            throw ArtificerUserException.derivedArtifactCreate(artifactType.getArtifactType());
        }
        if (artifactType.isDocument()) {
            throw new ArtificerUserException(Messages.i18n.format("INVALID_DOCARTY_CREATE"));
        }

        PersistenceManager persistenceManager = persistenceManager();
        // store the content
        BaseArtifactType persistedArtifact = persistenceManager.persistArtifact(artifact, null);

        Set<EventProducer> eventProducers = EventProducerFactory.getEventProducers();
        for (EventProducer eventProducer : eventProducers) {
            eventProducer.artifactCreated(persistedArtifact);
        }

        return persistedArtifact;
    }

    @Override
    public BaseArtifactType upload(String model, String type, String fileName, InputStream is)
            throws Exception {
        ArtifactType artifactType = ArtifactType.valueOf(model, type, true);
        return upload(artifactType, fileName, is);
    }

    @Override
    public BaseArtifactType upload(String fileName, InputStream is) throws Exception {
        return upload(null, fileName, is);
    }

    @Override
    public BaseArtifactType upload(ArtifactType artifactType, String fileName, InputStream is)
            throws Exception {
        // Pick a reasonable file name if Slug is not present
        if (fileName == null) {
            if (artifactType.getArtifactType() == ArtifactTypeEnum.Document) {
                fileName = "newartifact.bin";
            } else if (artifactType.getArtifactType() == ArtifactTypeEnum.XmlDocument) {
                fileName = "newartifact.xml";
            } else {
                fileName = "newartifact." + artifactType.getArtifactType().getModel();
            }
        }

        ArtifactContent content = null;
        ArchiveContext archiveContext = null;

        try {
            content = new ArtifactContent(fileName, is);
            if (ExtensionFactory.isArchive(content)) {
				// Artifact is an archive.  Create the context used throughout the process.
                archiveContext = ArchiveContext.createArchiveContext(content);

                if (artifactType == null) {
					// Attempt to auto-detect the archive type.
                    artifactType = ExtensionFactory.detect(content, archiveContext);
                }
            } else {
                if (artifactType == null) {
					// Attempt to auto-detect the artifact type.
                    artifactType = ExtensionFactory.detect(content);
                }
            }

			if (artifactType == null) {
				// Early exit.  No detector wanted it, and we don't return general Documents.
				return null;
			}

			if (artifactType.isDerived()) {
				throw ArtificerUserException.derivedArtifactCreate(artifactType.getArtifactType());
			}

			if (!artifactType.isDocument()) {
				throw new ArtificerUserException(Messages.i18n.format("INVALID_DOCARTY_CREATE"));
			}

			PersistenceManager persistenceManager = persistenceManager();

            // Important to do this *after* creating ArtifactContent.  Tika does not clone the InputStream!
            String mimeType = MimeTypes.determineMimeType(fileName, content.getInputStream(), artifactType);
            artifactType.setMimeType(mimeType);

            BaseArtifactType artifact = artifactType.newArtifactInstance();
            artifact.setName(fileName);

			BatchCreate creates = new BatchCreate();

			if (archiveContext != null) {
                // If it's an archive, expand it and upload through a batch (necessary for adequate relationship processing).

				// The parent UUID is necessary for the expandedFromDocument relationship.
				String parentUuid = UUID.randomUUID().toString();
				artifact.setUuid(parentUuid);

                // Expand (building up the batch).
                // Set the artifact in the context for the type detectors to use.
                archiveContext.setArchiveArtifactType(artifactType);
                Collection<File> subFiles = archiveContext.expand();
                for (File subFile : subFiles) {
                    String pathInArchive = archiveContext.stripWorkDir(subFile.getAbsolutePath());
                    ArtifactContent subArtifactContent = new ArtifactContent(pathInArchive, subFile);
                    if (ExtensionFactory.allowExpansionFromArchive(subArtifactContent, archiveContext)) {
                        ArtifactType subArtifactType = ExtensionFactory.detect(subArtifactContent, archiveContext);
                        // detectors do not accept everything...
                        if (subArtifactType != null) {
                            String subMimeType = MimeTypes.determineMimeType(subFile.getName(),
                                    subArtifactContent.getInputStream(), subArtifactType);
                            subArtifactType.setMimeType(subMimeType);

                            BaseArtifactType subArtifact = subArtifactType.newArtifactInstance();
                            subArtifact.setName(subFile.getName());

                            // set relevant properties/relationships
                            subArtifact.getOtherAttributes().put(
                                    ArtificerConstants.ARTIFICER_EXPANDED_FROM_ARCHIVE_UUID_QNAME, parentUuid);
                            subArtifact.getOtherAttributes().put(
                                    ArtificerConstants.ARTIFICER_EXPANDED_FROM_ARCHIVE_PATH_QNAME, pathInArchive);

                            creates.add(subArtifact, subArtifactContent, subArtifactContent.getPath());
                        }
                    }
                }
            }

			// Persist the primary artifact or archive.
			artifact = persistenceManager.persistArtifact(artifact, content);
			doUploadEvent(artifact);

			// Persist the batch, if there was one.
			List<BaseArtifactType> results = creates.execute(persistenceManager);
			for (BaseArtifactType result : results) {
				doUploadEvent(result);
			}

            return artifact;
        } finally {
            if (content != null) {
                content.cleanup();
            }
            if (archiveContext != null) {
                archiveContext.cleanup();
            }
        }
    }

	private void doUploadEvent(BaseArtifactType artifact) {
		Set<EventProducer> eventProducers = EventProducerFactory.getEventProducers();
		for (EventProducer eventProducer : eventProducers) {
			eventProducer.artifactCreated(artifact);
		}
	}

    @Override
    public BaseArtifactType upload(String model, String type, String fileName, byte[] contentBytes)
            throws Exception {
        return upload(model, type, fileName, new ByteArrayInputStream(contentBytes));
    }

    @Override
    public BaseArtifactType upload(String fileName, byte[] contentBytes) throws Exception {
        return upload(fileName, new ByteArrayInputStream(contentBytes));
    }

    @Override
    public BaseArtifactType upload(ArtifactType artifactType, String fileName, byte[] contentBytes)
            throws Exception {
        return upload(artifactType, fileName, new ByteArrayInputStream(contentBytes));
    }

    @Override
    public void updateMetaData(String model, String type, String uuid, BaseArtifactType updatedArtifact)
            throws Exception {
        ArtifactType artifactType = ArtifactType.valueOf(model, type, null);
        updateMetaData(artifactType, uuid, updatedArtifact);
    }

    @Override
    public void updateMetaData(BaseArtifactType updatedArtifact) throws Exception {
        ArtifactType artifactType = ArtifactType.valueOf(updatedArtifact);
        updateMetaData(artifactType, updatedArtifact.getUuid(), updatedArtifact);
    }

    @Override
    public void updateMetaData(ArtifactType artifactType, String uuid,
            BaseArtifactType updatedArtifact) throws Exception {
        PersistenceManager persistenceManager = persistenceManager();
        BaseArtifactType oldArtifact = persistenceManager.getArtifact(uuid, artifactType);
        if (oldArtifact == null) {
            throw ArtificerNotFoundException.artifactNotFound(uuid);
        }

        ArtifactVerifier verifier = new ArtifactVerifier(oldArtifact, artifactType);
        ArtifactVisitorHelper.visitArtifact(verifier, updatedArtifact);
        verifier.throwError();

        updatedArtifact = persistenceManager.updateArtifact(updatedArtifact, artifactType);

        Set<EventProducer> eventProducers = EventProducerFactory.getEventProducers();
        for (EventProducer eventProducer : eventProducers) {
            eventProducer.artifactUpdated(updatedArtifact, oldArtifact);
        }
    }

    @Override
    public BaseArtifactType addComment(ArtifactType artifactType, String uuid, String text) throws Exception {
        return persistenceManager().addComment(uuid, artifactType, text);
    }

    @Override
    public BaseArtifactType getMetaData(String model, String type, String uuid) throws Exception {
        ArtifactType artifactType = ArtifactType.valueOf(model, type, false);
        return getMetaData(artifactType, uuid);
    }

    @Override
    public BaseArtifactType getMetaData(ArtifactType artifactType, String uuid) throws Exception {
        PersistenceManager persistenceManager = persistenceManager();

        // Get the artifact by UUID
        // TODO: The last extendedDocFix check should not be necessary.  However, since we
        // don't know whether or not the artifact has content prior to calling ArtifactType.valueOf, this is
        // necessary.  It would be better if we could somehow get the artifact without knowing the artifact type
        // ahead of time (ie, purely use the JCR property).
        BaseArtifactType artifact = persistenceManager.getArtifact(uuid, artifactType);
        if (artifact == null || (!artifactType.getArtifactType().getApiType().equals(artifact.getArtifactType())
                && !(artifactType.getArtifactType().equals(ArtifactTypeEnum.ExtendedArtifactType) && artifact.getArtifactType().equals(BaseArtifactEnum.EXTENDED_DOCUMENT)))) {
            throw ArtificerNotFoundException.artifactNotFound(uuid);
        }

        return artifact;
    }

    @Override
    public InputStream getContent(String model, String type, String uuid) throws Exception {
        ArtifactType artifactType = ArtifactType.valueOf(model, type, true);
        BaseArtifactType artifact = getMetaData(artifactType, uuid);
        return getContent(artifactType, artifact);
    }

    @Override
    public InputStream getContent(ArtifactType artifactType, String uuid) throws Exception {
        BaseArtifactType artifact = getMetaData(artifactType, uuid);
        return getContent(artifactType, artifact);
    }

    @Override
    public InputStream getContent(ArtifactType artifactType, BaseArtifactType artifact) throws Exception {
        if (!(artifact instanceof DocumentArtifactType)) {
            throw ArtificerNotFoundException.contentNotFound(artifact.getUuid());
        }
        DocumentArtifactType documentArtifact = (DocumentArtifactType) artifact;
        if (documentArtifact.getContentSize() == 0  || StringUtils.isEmpty(documentArtifact.getContentHash())) {
            throw ArtificerNotFoundException.contentNotFound(artifact.getUuid());
        }

        PersistenceManager persistenceManager = persistenceManager();

        ArtifactContentTypeVisitor ctVizzy = new ArtifactContentTypeVisitor();
        ArtifactVisitorHelper.visitArtifact(ctVizzy, artifact);
        javax.ws.rs.core.MediaType mediaType = ctVizzy.getContentType();
        artifactType.setMimeType(mediaType.toString());
        return persistenceManager.getArtifactContent(artifact.getUuid(), artifactType);
    }

    @Override
    public byte[] getContentBytes(String model, String type, String uuid) throws Exception {
        return IOUtils.toByteArray(getContent(model, type, uuid));
    }

    @Override
    public byte[] getContentBytes(ArtifactType artifactType, String uuid) throws Exception {
        return IOUtils.toByteArray(getContent(artifactType, uuid));
    }

    @Override
    public byte[] getContentBytes(ArtifactType artifactType, BaseArtifactType artifact) throws Exception {
        return IOUtils.toByteArray(getContent(artifactType, artifact));
    }

    @Override
    public void delete(String model, String type, String uuid) throws Exception {
        delete(model, type, uuid, false);
    }

    @Override
    public void delete(String model, String type, String uuid, boolean force) throws Exception {
        ArtifactType artifactType = ArtifactType.valueOf(model, type, null);
        delete(artifactType, uuid, force);
    }

    @Override
    public void delete(ArtifactType artifactType, String uuid) throws Exception {
        delete(artifactType, uuid, false);
    }

    @Override
    public void delete(ArtifactType artifactType, String uuid, boolean force) throws Exception {
        if (artifactType.isDerived()) {
            throw ArtificerUserException.derivedArtifactDelete(artifactType.getArtifactType());
        }

        PersistenceManager persistenceManager = persistenceManager();
        // Delete the artifact by UUID
        BaseArtifactType artifact = persistenceManager.deleteArtifact(uuid, artifactType, force);

        Set<EventProducer> eventProducers = EventProducerFactory.getEventProducers();
        for (EventProducer eventProducer : eventProducers) {
            eventProducer.artifactDeleted(artifact);
        }
    }
}
