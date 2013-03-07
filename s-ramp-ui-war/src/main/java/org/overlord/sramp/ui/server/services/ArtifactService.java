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
package org.overlord.sramp.ui.server.services;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.jboss.errai.bus.server.annotations.Service;
import org.overlord.sramp.atom.err.SrampAtomException;
import org.overlord.sramp.client.SrampClientException;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.common.SrampModelUtils;
import org.overlord.sramp.ui.client.shared.beans.ArtifactBean;
import org.overlord.sramp.ui.client.shared.exceptions.SrampUiException;
import org.overlord.sramp.ui.client.shared.services.IArtifactService;
import org.overlord.sramp.ui.server.api.SrampApiClientAccessor;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.DocumentArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.Property;

/**
 * Concrete implementation of the artifact service.
 *
 * @author eric.wittmann@redhat.com
 */
@Service
public class ArtifactService implements IArtifactService {

    // Limit content grabs to 2mb
    private static final Long TWO_MEG = 2l * 1024l * 1024l;

    @Inject
    private SrampApiClientAccessor clientAccessor;

    /**
     * Constructor.
     */
    public ArtifactService() {
    }

    /**
     * @see org.overlord.sramp.ui.client.shared.services.IArtifactService#get(java.lang.String)
     */
    @Override
    public ArtifactBean get(String uuid) throws SrampUiException {
        try {
            BaseArtifactType artifact = clientAccessor.getClient().getArtifactMetaData(uuid);
            ArtifactType artifactType = ArtifactType.valueOf(artifact);

            ArtifactBean bean = new ArtifactBean();
            bean.setModel(artifactType.getArtifactType().getModel());
            bean.setType(artifactType.getArtifactType().getType());
            bean.setUuid(artifact.getUuid());
            bean.setName(artifact.getName());
            bean.setDescription(artifact.getDescription());
            bean.setVersion(artifact.getVersion());
            bean.setCreatedBy(artifact.getCreatedBy());
            bean.setCreatedOn(artifact.getCreatedTimestamp().toGregorianCalendar().getTime());
            bean.setUpdatedOn(artifact.getLastModifiedTimestamp().toGregorianCalendar().getTime());
            bean.setUpdatedBy(artifact.getLastModifiedBy());
            bean.setDerived(artifactType.getArtifactType().isDerived());
            if (SrampModelUtils.isDocumentArtifact(artifact)) {
                DocumentArtifactType doc = (DocumentArtifactType) artifact;
                bean.setContentSize(doc.getContentSize());
                bean.setContentType(doc.getContentType());
                if  (SrampModelUtils.isTextDocumentArtifact(doc)) {
                    bean.setTextDocument(true);
                }
            }
            // Properties
            for (Property property : artifact.getProperty()) {
                bean.setProperty(property.getPropertyName(), property.getPropertyValue());
            }
            // Classifications
            bean.getClassifiedBy().addAll(artifact.getClassifiedBy());
            // Num Relationships
            int numRelationships = artifact.getRelationship() == null ? 0 : artifact.getRelationship().size();
            bean.setNumRelationships(numRelationships);

            return bean;
        } catch (SrampClientException e) {
            throw new SrampUiException(e.getMessage());
        } catch (SrampAtomException e) {
            throw new SrampUiException(e.getMessage());
        }
    }

    /**
     * @see org.overlord.sramp.ui.client.shared.services.IArtifactService#getDocumentContent(java.lang.String, java.lang.String)
     */
    @Override
    public String getDocumentContent(String uuid, String artifactType) throws SrampUiException {
        try {
            ArtifactType at = ArtifactType.valueOf(artifactType);
            BaseArtifactType artifact = clientAccessor.getClient().getArtifactMetaData(at, uuid);
            String response = "N/A (Please download the content instead...)";
            if (SrampModelUtils.isDocumentArtifact(artifact)) {
                DocumentArtifactType doc = (DocumentArtifactType) artifact;
                if (SrampModelUtils.isTextDocumentArtifact(doc) && doc.getContentSize() <= TWO_MEG) {
                    InputStream content = null;
                    try {
                        content = clientAccessor.getClient().getArtifactContent(at, uuid);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        IOUtils.copy(content, baos);
                        // TODO: obey the document's encoding here (if we can find it) but default to UTF-8
                        response = baos.toString("UTF-8");
                    } finally {
                        IOUtils.closeQuietly(content);
                    }
                }
            }
            return response;
        } catch (Exception e) {
            throw new SrampUiException(e.getMessage());
        }
    }

    /**
     * @see org.overlord.sramp.ui.client.shared.services.IArtifactService#update(org.overlord.sramp.ui.client.shared.beans.ArtifactBean)
     */
    @Override
    public void update(ArtifactBean artifact) throws SrampUiException {
        // TODO implement this!
        throw new SrampUiException("Not yet implemented.");
    }

}
