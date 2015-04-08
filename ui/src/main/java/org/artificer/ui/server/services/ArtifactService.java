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
package org.artificer.ui.server.services;

import org.apache.commons.io.IOUtils;
import org.artificer.client.ArtificerClientException;
import org.artificer.common.ArtifactType;
import org.artificer.common.ArtificerModelUtils;
import org.artificer.common.error.ArtificerServerException;
import org.artificer.ui.client.shared.beans.ArtifactBean;
import org.artificer.ui.client.shared.beans.ArtifactCommentBean;
import org.artificer.ui.client.shared.beans.ArtifactRelationshipsIndexBean;
import org.artificer.ui.client.shared.exceptions.ArtificerUiException;
import org.artificer.ui.client.shared.services.IArtifactService;
import org.artificer.ui.server.api.ArtificerApiClientAccessor;
import org.artificer.ui.server.i18n.Messages;
import org.artificer.ui.server.services.util.RelationshipResolver;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Comment;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DocumentArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Property;

import javax.enterprise.context.ApplicationScoped;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

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
     * @see org.artificer.ui.client.shared.services.IArtifactService#get(java.lang.String)
     */
    @Override
    public ArtifactBean get(String uuid) throws ArtificerUiException {
        try {
            BaseArtifactType artifact = ArtificerApiClientAccessor.getClient().getArtifactMetaData(uuid);
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
            if (ArtificerModelUtils.isDocumentArtifact(artifact)) {
                DocumentArtifactType doc = (DocumentArtifactType) artifact;
                bean.setContentSize(doc.getContentSize());
                bean.setContentType(doc.getContentType());
                if  (ArtificerModelUtils.isTextDocumentArtifact(doc)) {
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

            // Comments
            for (Comment comment : artifact.getComment()) {
                bean.getComments().add(toCommentBean(comment));
            }

            return bean;
        } catch (ArtificerClientException e) {
            throw new ArtificerUiException(e.getMessage());
        } catch (ArtificerServerException e) {
            throw new ArtificerUiException(e.getMessage());
        }
    }

    private ArtifactCommentBean toCommentBean(Comment comment) {
        ArtifactCommentBean commentBean = new ArtifactCommentBean();
        commentBean.setCreatedBy(comment.getCreatedBy());
        commentBean.setCreatedOn(comment.getCreatedTimestamp().toGregorianCalendar().getTime());
        commentBean.setText(comment.getText());
        return commentBean;
    }

    /**
     * @see org.artificer.ui.client.shared.services.IArtifactService#getDocumentContent(java.lang.String, java.lang.String)
     */
    @Override
    public String getDocumentContent(String uuid, String artifactType) throws ArtificerUiException {
        try {
            ArtifactType at = ArtifactType.valueOf(artifactType);
            BaseArtifactType artifact = ArtificerApiClientAccessor.getClient().getArtifactMetaData(at, uuid);
            String response = Messages.i18n.format("ArtifactService.DownloadContent"); //$NON-NLS-1$
            if (ArtificerModelUtils.isDocumentArtifact(artifact)) {
                DocumentArtifactType doc = (DocumentArtifactType) artifact;
                if (ArtificerModelUtils.isTextDocumentArtifact(doc) && doc.getContentSize() <= TWO_MEG) {
                    InputStream content = null;
                    try {
                        content = ArtificerApiClientAccessor.getClient().getArtifactContent(at, uuid);
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
            throw new ArtificerUiException(e.getMessage());
        }
    }

    /**
     * @see org.artificer.ui.client.shared.services.IArtifactService#getRelationships(java.lang.String, java.lang.String)
     */
    @Override
    public ArtifactRelationshipsIndexBean getRelationships(String uuid, String artifactType)
            throws ArtificerUiException {
        ArtifactRelationshipsIndexBean rval = new ArtifactRelationshipsIndexBean();
        try {
            ArtifactType at = ArtifactType.valueOf(artifactType);
            BaseArtifactType artifact = ArtificerApiClientAccessor.getClient().getArtifactMetaData(at, uuid);
            RelationshipResolver relResolver = new RelationshipResolver(ArtificerApiClientAccessor.getClient(), rval);
            relResolver.resolveAll(artifact);
        } catch (ArtificerClientException e) {
            throw new ArtificerUiException(e.getMessage());
        } catch (ArtificerServerException e) {
            throw new ArtificerUiException(e.getMessage());
        }

        return rval;
    }

    /**
     * @see org.artificer.ui.client.shared.services.IArtifactService#update(org.artificer.ui.client.shared.beans.ArtifactBean)
     */
    @Override
    public void update(ArtifactBean bean) throws ArtificerUiException {
        try {
            ArtifactType artifactType = ArtifactType.valueOf(bean.getModel(), bean.getRawType(), null);
            // Grab the latest from the server
            BaseArtifactType artifact = ArtificerApiClientAccessor.getClient().getArtifactMetaData(artifactType, bean.getUuid());
            // Update it with new data from the bean
            artifact.setName(bean.getName());
            artifact.setDescription(bean.getDescription());
            artifact.setVersion(bean.getVersion());
            artifact.getProperty().clear();
            for (String propName : bean.getPropertyNames()) {
                ArtificerModelUtils.setCustomProperty(artifact, propName, bean.getProperty(propName));
            }
            artifact.getClassifiedBy().clear();
            for (String classifier : bean.getClassifiedBy()) {
                artifact.getClassifiedBy().add(classifier);
            }
            // Push the changes back to the server
            ArtificerApiClientAccessor.getClient().updateArtifactMetaData(artifact);
        } catch (ArtificerClientException e) {
            throw new ArtificerUiException(e.getMessage());
        } catch (ArtificerServerException e) {
            throw new ArtificerUiException(e.getMessage());
        }
    }

    @Override
    public ArtifactCommentBean addComment(String uuid, String artifactType, String text) throws ArtificerUiException {
        try {
            ArtifactType at = ArtifactType.valueOf(artifactType);
            BaseArtifactType artifact = ArtificerApiClientAccessor.getClient().addComment(uuid, at, text);
            List<Comment> comments = artifact.getComment();
            // latest should be last
            return(toCommentBean(comments.get(comments.size() - 1)));
        } catch (ArtificerClientException e) {
            throw new ArtificerUiException(e.getMessage());
        } catch (ArtificerServerException e) {
            throw new ArtificerUiException(e.getMessage());
        }
    }

    /**
     * @see org.artificer.ui.client.shared.services.IArtifactService#delete(org.artificer.ui.client.shared.beans.ArtifactBean)
     */
    @Override
    public void delete(ArtifactBean bean) throws ArtificerUiException {
        try {
            ArtifactType artifactType = ArtifactType.valueOf(bean.getModel(), bean.getRawType(), null);
            ArtificerApiClientAccessor.getClient().deleteArtifact(bean.getUuid(), artifactType);
        } catch (ArtificerClientException e) {
            throw new ArtificerUiException(e.getMessage());
        } catch (ArtificerServerException e) {
            throw new ArtificerUiException(e.getMessage());
        }
    }

    /**
     * Creates a link to the remote repository for the given artifact.
     * @param artifact
     * @param artifactType 
     */
    private String getRepositoryLink(BaseArtifactType artifact, ArtifactType artifactType) {
        StringBuilder builder = new StringBuilder();
        String endpoint = ArtificerApiClientAccessor.getClient().getEndpoint();
        builder.append(endpoint);
        if (!endpoint.endsWith("/")) { //$NON-NLS-1$
            builder.append("/"); //$NON-NLS-1$
        }
        builder.append("s-ramp/");
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
