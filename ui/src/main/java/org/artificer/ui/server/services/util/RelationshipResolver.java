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
package org.artificer.ui.server.services.util;

import org.artificer.client.ArtificerAtomApiClient;
import org.artificer.common.query.ArtifactSummary;
import org.artificer.client.query.QueryResultSet;
import org.artificer.common.ArtifactType;
import org.artificer.common.ArtificerConstants;
import org.artificer.common.visitors.ArtifactVisitorHelper;
import org.artificer.common.visitors.RelationshipArtifactVisitor;
import org.artificer.ui.client.shared.beans.ArtifactRelationshipBean;
import org.artificer.ui.client.shared.beans.ArtifactRelationshipsIndexBean;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Target;

import java.util.Iterator;

/**
 * Visits an artifact to resolve all of its relationships.
 *
 * @author eric.wittmann@redhat.com
 */
public class RelationshipResolver {

    private ArtificerAtomApiClient client;
    private ArtifactRelationshipsIndexBean indexedRelationships;

    /**
     * Constructor.
     * @param client
     * @param indexedRelationships
     */
    public RelationshipResolver(ArtificerAtomApiClient client, ArtifactRelationshipsIndexBean indexedRelationships) {
        this.client = client;
        this.indexedRelationships = indexedRelationships;
    }

    /**
     * @param artifact
     */
    public void resolveAll(BaseArtifactType artifact) {
        // relationships originating from artifact
        ArtifactVisitorHelper.visitArtifact(new RelationshipArtifactVisitor() {
            @Override
            protected void visitRelationship(String type, Target target, boolean generic) {
                if (target == null)
                    return;
                String targetUuid = target.getValue();
                try {
                    BaseArtifactType targetArtifact = client.getArtifactMetaData(targetUuid);
                    ArtifactType targetArtifactType = ArtifactType.valueOf(targetArtifact);
                    ArtifactRelationshipBean bean = new ArtifactRelationshipBean();
                    bean.setRelationshipType(type);
                    bean.setTargetDerived(targetArtifactType.isDerived());
                    bean.setTargetName(targetArtifact.getName());
                    bean.setTargetUuid(targetUuid);
                    bean.setTargetType(targetArtifactType.getType());
                    bean.setRelationshipGeneric(generic);
                    indexedRelationships.addRelationship(bean);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                    // TODO handle the error case here?  what to do?
                }
            }
        }, artifact);

        try {
            // relationships *targeting* artifact ("reverse")
            QueryResultSet results = client.reverseRelationships(artifact.getUuid());
            Iterator<ArtifactSummary> itr = results.iterator();
            while (itr.hasNext()) {
                ArtifactSummary sourceArtifactSummary = itr.next();
                ArtifactRelationshipBean bean = new ArtifactRelationshipBean();
                bean.setRelationshipType((String) sourceArtifactSummary.getExtensionAttribute(
                        ArtificerConstants.ARTIFICER_RELATIONSHIP_TYPE_QNAME));
                String generic = (String) sourceArtifactSummary.getExtensionAttribute(
                        ArtificerConstants.ARTIFICER_RELATIONSHIP_GENERIC_QNAME);
                bean.setRelationshipGeneric(Boolean.valueOf(generic));
                bean.setTargetDerived(sourceArtifactSummary.getArtifactType().isDerived());
                bean.setTargetName(sourceArtifactSummary.getName());
                bean.setTargetUuid(sourceArtifactSummary.getUuid());
                bean.setTargetType(sourceArtifactSummary.getType());
                indexedRelationships.addReverseRelationship(bean);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
            // TODO handle the error case here?  what to do?
        }
    }

}
