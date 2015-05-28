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
import org.artificer.ui.client.shared.beans.ArtifactRelationshipBean;
import org.artificer.ui.client.shared.beans.ArtifactRelationshipsBean;
import org.artificer.ui.client.shared.beans.ArtifactRelationshipsIndexBean;
import org.artificer.ui.client.shared.beans.ArtifactSummaryBean;
import org.artificer.ui.client.shared.beans.RelationshipGraphBean;
import org.artificer.ui.client.shared.beans.RelationshipTreeBean;
import org.artificer.ui.client.shared.exceptions.ArtificerUiException;
import org.artificer.ui.client.shared.services.IArtifactService;
import org.artificer.ui.server.api.ArtificerApiClientAccessor;
import org.artificer.ui.server.i18n.Messages;
import org.artificer.ui.server.services.util.RelationshipResolver;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Comment;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DocumentArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Property;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Relationship;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Target;

import javax.enterprise.context.ApplicationScoped;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

    @Override
    public ArtifactRelationshipsIndexBean getRelationships(String uuid)
            throws ArtificerUiException {
        ArtifactRelationshipsIndexBean rval = new ArtifactRelationshipsIndexBean();
        try {
            BaseArtifactType artifact = ArtificerApiClientAccessor.getClient().getArtifactMetaData(uuid);
            RelationshipResolver relResolver = new RelationshipResolver(ArtificerApiClientAccessor.getClient(), rval);
            relResolver.resolveAll(artifact);
        } catch (ArtificerClientException e) {
            throw new ArtificerUiException(e.getMessage());
        } catch (ArtificerServerException e) {
            throw new ArtificerUiException(e.getMessage());
        }

        return rval;
    }

    @Override
    public RelationshipGraphBean getRelationshipGraph(String startUuid) throws ArtificerUiException {
        RelationshipGraphBean rval = new RelationshipGraphBean();
        // To easily prevent duplicates, keep a second collection with processed UUIDs.
        List<String> processedUuids = new ArrayList<>();
        buildRelationshipGraph(startUuid, rval, processedUuids, true);
        return rval;
    }

    private RelationshipGraphBean buildRelationshipGraph(String uuid, RelationshipGraphBean rval,
            List<String> processedUuids, boolean isOriginal) throws ArtificerUiException {
        try {
            if (!processedUuids.contains(uuid)) {
                // We need to be smart (hard to believe, I know...) about what needs to be included in the graph and
                // what needs to be filtered out.  In general, allowing all depths of relationships causes chaos in the
                // graph.  To see what we actually need, take an XSD and WSDL example.  The WSDL imports the XSD and
                // its elements use the XSD type declarations.  If we view the graph for one of the type declarations,
                // we're only interested in seeing how the relationships flow through the WSDL elements that actually
                // use it, *not* every single derived artifact in the WSDL.  So, rules:
                //
                // 1.) Start with the artifact in question.
                // 2.) If #1 is primary, add its children using the *reverse* 'relatedDocument' relationships.
                // 3.) Based on the #1 starting point, build the rest of the graph, following *all* relationships
                //     through all levels.
                // 4.) If a relationship results in another *derived* node on the graph, also add its parent (using the
                //     'relatedDocument' relationship).
                // 5.) #2 and #4 should be the only cases when 'relatedDocument' is processed.  For all others, skip it.
                // Note about #4-#5: The main point is that if we arrive at a derived artifact, generally it's useful to
                //     see its parent and how that parent fits in.  However, the reverse is *not* true.  If a
                //     relationship points at a primary (ie, WSDL<-XSD), we only want to see the WSDL's derived
                //     artifacts if they somehow lead back to the XSD.  Otherwise, it just crowds the graph.

                processedUuids.add(uuid);

                // Generate the node
                BaseArtifactType artifact = ArtificerApiClientAccessor.getClient().getArtifactMetaData(uuid);
                ArtifactType artifactType = ArtifactType.valueOf(artifact);
                ArtifactSummaryBean bean = new ArtifactSummaryBean();
                bean.setModel(artifactType.getArtifactType().getModel());
                bean.setType(artifactType.getType());
                bean.setRawType(artifactType.getArtifactType().getType());
                bean.setUuid(artifact.getUuid());
                bean.setName(artifact.getName());
                bean.setDerived(artifactType.isDerived());

                // Resolve all of its relationships, both forward and reverse
                ArtifactRelationshipsIndexBean relIndex = new ArtifactRelationshipsIndexBean();
                RelationshipResolver relResolver = new RelationshipResolver(ArtificerApiClientAccessor.getClient(), relIndex);
                relResolver.resolveAll(artifact);

                for (ArtifactRelationshipsBean rels : relIndex.getRelationships().values()) {
                    for (ArtifactRelationshipBean rel : rels.getRelationships()) {
                        // Simply allow everything since, at this point, a 'relatedDocument' as a forward relationship
                        // implies this is derived.
                        buildRelationshipGraph(rel.getTargetUuid(), rval, processedUuids, false);
                    }
                }
                Iterator<Map.Entry<String, ArtifactRelationshipsBean>> reverseRelIt
                        = relIndex.getReverseRelationships().entrySet().iterator();
                while (reverseRelIt.hasNext()) {
                    Map.Entry<String, ArtifactRelationshipsBean> reverseRel = reverseRelIt.next();
                    if (!isOriginal && reverseRel.getKey().equalsIgnoreCase("relatedDocument")) {
                        // Skip processing and remove it from the index.
                        reverseRelIt.remove();
                    } else {
                        // Either this is the original artifact or the rel isn't 'relatedDocument'.  Process it...
                        for (ArtifactRelationshipBean rel : reverseRel.getValue().getRelationships()) {
                            buildRelationshipGraph(rel.getTargetUuid(), rval, processedUuids, false);
                        }
                    }
                }

                // add it to the map
                rval.add(bean, relIndex);
            }
            return rval;
        } catch (ArtificerClientException e) {
            throw new ArtificerUiException(e.getMessage());
        } catch (ArtificerServerException e) {
            throw new ArtificerUiException(e.getMessage());
        }
    }

    @Override
    public RelationshipTreeBean getRelationshipTree(String startUuid) throws ArtificerUiException {
        Map<String, ArtifactSummaryBean> artifactCache = new HashMap<>();
        Map<String, ArtifactRelationshipsIndexBean> relCache = new HashMap<>();
        return buildRelationshipTree(startUuid, null, false, "", artifactCache, relCache, true);
    }

    private RelationshipTreeBean buildRelationshipTree(String uuid, String relationshipType,
            boolean relationshipReverse, String path, Map<String, ArtifactSummaryBean> artifactCache,
            Map<String, ArtifactRelationshipsIndexBean> relCache, boolean isOriginal)
            throws ArtificerUiException {

        try {
            // Generate the node and relationships
            ArtifactSummaryBean artifactBean;
            ArtifactRelationshipsIndexBean relIndex;
            if (artifactCache.containsKey(uuid)) {
                artifactBean = artifactCache.get(uuid);
                relIndex = relCache.get(uuid);
            } else {
                BaseArtifactType artifact = ArtificerApiClientAccessor.getClient().getArtifactMetaData(uuid);
                ArtifactType artifactType = ArtifactType.valueOf(artifact);
                artifactBean = new ArtifactSummaryBean();
                artifactBean.setModel(artifactType.getArtifactType().getModel());
                artifactBean.setType(artifactType.getType());
                artifactBean.setRawType(artifactType.getArtifactType().getType());
                artifactBean.setUuid(artifact.getUuid());
                artifactBean.setName(artifact.getName());
                artifactBean.setDerived(artifactType.isDerived());

                relIndex = new ArtifactRelationshipsIndexBean();
                RelationshipResolver relResolver = new RelationshipResolver(ArtificerApiClientAccessor.getClient(), relIndex);
                relResolver.resolveAll(artifact);

                artifactCache.put(uuid, artifactBean);
                relCache.put(uuid, relIndex);
            }

            RelationshipTreeBean tree = new RelationshipTreeBean();
            tree.setArtifact(artifactBean);
            tree.setRelationshipType(relationshipType);
            tree.setRelationshipReverse(relationshipReverse);

            path += uuid;

            List<RelationshipTreeBean> children = new ArrayList<>();
            for (String key : relIndex.getRelationships().keySet()) {
                if (isOriginal || !"relatedDocument".equalsIgnoreCase(key)) {
                    for (ArtifactRelationshipBean rel : relIndex.getRelationships().get(key).getRelationships()) {
                        if (!path.contains(rel.getTargetUuid())) {
                            children.add(buildRelationshipTree(rel.getTargetUuid(), rel.getRelationshipType(), false, path,
                                    artifactCache, relCache, false));
                        }
                    }
                }
            }
            for (String key : relIndex.getReverseRelationships().keySet()) {
                if (!"relatedDocument".equalsIgnoreCase(key)) {
                    for (ArtifactRelationshipBean rel : relIndex.getReverseRelationships().get(key).getRelationships()) {
                        if (!path.contains(rel.getTargetUuid())) {
                            children.add(buildRelationshipTree(rel.getTargetUuid(), rel.getRelationshipType(), true, path,
                                    artifactCache, relCache, false));
                        }
                    }
                }
            }
            tree.setChildren(children);

            return tree;
        } catch (ArtificerClientException e) {
            throw new ArtificerUiException(e.getMessage());
        } catch (ArtificerServerException e) {
            throw new ArtificerUiException(e.getMessage());
        }
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

    @Override
    public void addRelationships(String sourceUuid, ArtifactRelationshipsBean relationships)
            throws ArtificerUiException {
        try {
            BaseArtifactType artifact = ArtificerApiClientAccessor.getClient().getArtifactMetaData(sourceUuid);
            for (ArtifactRelationshipBean relationshipBean : relationships.getRelationships()) {
                Relationship relationship = new Relationship();
                relationship.setRelationshipType(relationshipBean.getRelationshipType());
                Target target = new Target();
                target.setValue(relationshipBean.getTargetUuid());
                relationship.getRelationshipTarget().add(target);
                artifact.getRelationship().add(relationship);
            }
            ArtificerApiClientAccessor.getClient().updateArtifactMetaData(artifact);
        } catch (ArtificerClientException e) {
            throw new ArtificerUiException(e.getMessage());
        } catch (ArtificerServerException e) {
            throw new ArtificerUiException(e.getMessage());
        }
    }

    @Override
    public void editRelationship(String relationshipType, String newRelationshipType, String sourceUuid, String targetUuid)
            throws ArtificerUiException {
        try {
            BaseArtifactType artifact = ArtificerApiClientAccessor.getClient().getArtifactMetaData(sourceUuid);

            doDeleteRelationship(artifact, relationshipType, targetUuid);

            Relationship relationship = new Relationship();
            relationship.setRelationshipType(newRelationshipType);
            Target target = new Target();
            target.setValue(targetUuid);
            relationship.getRelationshipTarget().add(target);
            artifact.getRelationship().add(relationship);

            ArtificerApiClientAccessor.getClient().updateArtifactMetaData(artifact);
        } catch (ArtificerClientException e) {
            throw new ArtificerUiException(e.getMessage());
        } catch (ArtificerServerException e) {
            throw new ArtificerUiException(e.getMessage());
        }
    }

    @Override
    public void deleteRelationship(String relationshipType, String sourceUuid, String targetUuid)
            throws ArtificerUiException {
        try {
            BaseArtifactType artifact = ArtificerApiClientAccessor.getClient().getArtifactMetaData(sourceUuid);
            doDeleteRelationship(artifact, relationshipType, targetUuid);
            ArtificerApiClientAccessor.getClient().updateArtifactMetaData(artifact);
        } catch (ArtificerClientException e) {
            throw new ArtificerUiException(e.getMessage());
        } catch (ArtificerServerException e) {
            throw new ArtificerUiException(e.getMessage());
        }
    }

    private void doDeleteRelationship(BaseArtifactType artifact, String relationshipType, String targetUuid) {
        for (Relationship relationship : artifact.getRelationship()) {
            if (relationship.getRelationshipType().equals(relationshipType)) {
                Iterator<Target> itr = relationship.getRelationshipTarget().iterator();
                while (itr.hasNext()) {
                    Target target = itr.next();
                    if (target.getValue().equals(targetUuid)) {
                        itr.remove();
                        break;
                    }
                }
            }
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
