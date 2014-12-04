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

import javax.enterprise.context.ApplicationScoped;

import org.apache.commons.io.IOUtils;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DocumentArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Property;
import org.overlord.sramp.atom.err.SrampAtomException;
import org.overlord.sramp.client.SrampClientException;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.common.SrampModelUtils;
import org.overlord.sramp.ui.client.shared.beans.ArtifactBean;
import org.overlord.sramp.ui.client.shared.beans.ArtifactRelationshipsIndexBean;
import org.overlord.sramp.ui.client.shared.exceptions.SrampUiException;
import org.overlord.sramp.ui.client.shared.services.IArtifactService;
import org.overlord.sramp.ui.server.api.SrampApiClientAccessor;
import org.overlord.sramp.ui.server.i18n.Messages;
import org.overlord.sramp.ui.server.services.util.RelationshipResolver;

/**
 * Concrete implementation of the artifact service.
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class ArtifactService implements IArtifactService {

    // Limit content grabs to 2mb
    private static final Long TWO_MEG = 2l * 1024l * 1024l;

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
            BaseArtifactType artifact = SrampApiClientAccessor.getClient().getArtifactMetaData(uuid);
            ArtifactType artifactType = ArtifactType.valueOf(artifact);
            
            ArtifactBean bean = new ArtifactBean();
            bean.setModel(artifactType.getArtifactType().getModel());
            bean.setType(artifactType.getType());
            bean.setRawType(artifactType.getArtifactType().getType());
            bean.setUuid(artifact.getUuid());
            bean.setName(artifact.getName());
            bean.setDescription(artifact.getDescription());
            bean.setVersion(artifact.getVersion());
            bean.setCreatedBy(artifact.getCreatedBy());
            bean.setCreatedOn(artifact.getCreatedTimestamp().toGregorianCalendar().getTime());
            bean.setUpdatedOn(artifact.getLastModifiedTimestamp().toGregorianCalendar().getTime());
            bean.setUpdatedBy(artifact.getLastModifiedBy());
            bean.setDerived(artifactType.isDerived());
            bean.setRepositoryLink(getRepositoryLink(artifact, artifactType));
            bean.setRepositoryMediaLink(getRepositoryMediaLink(artifact, artifactType));
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
            BaseArtifactType artifact = SrampApiClientAccessor.getClient().getArtifactMetaData(at, uuid);
            String response = Messages.i18n.format("ArtifactService.DownloadContent"); //$NON-NLS-1$
            if (SrampModelUtils.isDocumentArtifact(artifact)) {
                DocumentArtifactType doc = (DocumentArtifactType) artifact;
                if (SrampModelUtils.isTextDocumentArtifact(doc) && doc.getContentSize() <= TWO_MEG) {
                    InputStream content = null;
                    try {
                        content = SrampApiClientAccessor.getClient().getArtifactContent(at, uuid);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        IOUtils.copy(content, baos);
                        // TODO: obey the document's encoding here (if we can find it) but default to UTF-8
                        response = baos.toString("UTF-8"); //$NON-NLS-1$
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
     * @see org.overlord.sramp.ui.client.shared.services.IArtifactService#getRelationships(java.lang.String, java.lang.String)
     */
    @Override
    public ArtifactRelationshipsIndexBean getRelationships(String uuid, String artifactType)
            throws SrampUiException {
        ArtifactRelationshipsIndexBean rval = new ArtifactRelationshipsIndexBean();
        try {
            ArtifactType at = ArtifactType.valueOf(artifactType);
            BaseArtifactType artifact = SrampApiClientAccessor.getClient().getArtifactMetaData(at, uuid);
            RelationshipResolver relResolver = new RelationshipResolver(SrampApiClientAccessor.getClient(), rval);
            relResolver.resolveAll(artifact);
        } catch (SrampClientException e) {
            throw new SrampUiException(e.getMessage());
        } catch (SrampAtomException e) {
            throw new SrampUiException(e.getMessage());
        }

        return rval;
    }

    /**
     * @see org.overlord.sramp.ui.client.shared.services.IArtifactService#update(org.overlord.sramp.ui.client.shared.beans.ArtifactBean)
     */
    @Override
    public void update(ArtifactBean bean) throws SrampUiException {
        try {
            ArtifactType artifactType = ArtifactType.valueOf(bean.getModel(), bean.getRawType(), null);
            // Grab the latest from the server
            BaseArtifactType artifact = SrampApiClientAccessor.getClient().getArtifactMetaData(artifactType, bean.getUuid());
            // Update it with new data from the bean
            artifact.setName(bean.getName());
            artifact.setDescription(bean.getDescription());
            artifact.setVersion(bean.getVersion());
            artifact.getProperty().clear();
            for (String propName : bean.getPropertyNames()) {
                SrampModelUtils.setCustomProperty(artifact, propName, bean.getProperty(propName));
            }
            artifact.getClassifiedBy().clear();
            for (String classifier : bean.getClassifiedBy()) {
                artifact.getClassifiedBy().add(classifier);
            }
            // Push the changes back to the server
            SrampApiClientAccessor.getClient().updateArtifactMetaData(artifact);
        } catch (SrampClientException e) {
            throw new SrampUiException(e.getMessage());
        } catch (SrampAtomException e) {
            throw new SrampUiException(e.getMessage());
        }
    }

    /**
     * @see org.overlord.sramp.ui.client.shared.services.IArtifactService#delete(org.overlord.sramp.ui.client.shared.beans.ArtifactBean)
     */
    @Override
    public void delete(ArtifactBean bean) throws SrampUiException {
        try {
            ArtifactType artifactType = ArtifactType.valueOf(bean.getModel(), bean.getRawType(), null);
            SrampApiClientAccessor.getClient().deleteArtifact(bean.getUuid(), artifactType);
        } catch (SrampClientException e) {
            throw new SrampUiException(e.getMessage());
        } catch (SrampAtomException e) {
            throw new SrampUiException(e.getMessage());
        }
    }

    /**
     * Creates a link to the remote repository for the given artifact.
     * @param artifact
     * @param artifactType 
     */
    private String getRepositoryLink(BaseArtifactType artifact, ArtifactType artifactType) {
        StringBuilder builder = new StringBuilder();
        String endpoint = SrampApiClientAccessor.getClient().getEndpoint();
        builder.append(endpoint);
        if (!endpoint.endsWith("/")) { //$NON-NLS-1$
            builder.append("/"); //$NON-NLS-1$
        }
        builder.append(artifactType.getModel());
        builder.append("/"); //$NON-NLS-1$
        builder.append(artifactType.getType());
        builder.append("/"); //$NON-NLS-1$
        builder.append(artifact.getUuid());
        return builder.toString();
    }

    /**
     * Creates a media link to the remote repository for the given artifact.
     * @param artifact
     * @param artifactType 
     */
    private String getRepositoryMediaLink(BaseArtifactType artifact, ArtifactType artifactType) {
        return getRepositoryLink(artifact, artifactType) + "/media"; //$NON-NLS-1$
    }

}
