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

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Target;
import org.artificer.client.ArtificerAtomApiClient;
import org.artificer.common.ArtifactType;
import org.artificer.common.visitors.ArtifactVisitorHelper;
import org.artificer.common.visitors.RelationshipArtifactVisitor;
import org.artificer.ui.client.shared.beans.ArtifactRelationshipBean;
import org.artificer.ui.client.shared.beans.ArtifactRelationshipsIndexBean;

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
        ArtifactVisitorHelper.visitArtifact(new RelationshipArtifactVisitor() {
            @Override
            protected void visitRelationship(String type, Target target) {
                if (target == null)
                    return;
                String targetUuid = target.getValue();
                try {
                    BaseArtifactType targetArtifact = client.getArtifactMetaData(targetUuid);
                    ArtifactType targetArtifactType = ArtifactType.valueOf(targetArtifact);
                    ArtifactRelationshipBean bean = new ArtifactRelationshipBean();
                    bean.setRelationshipType(type);
                    bean.setTargetLastModified(targetArtifact.getLastModifiedTimestamp().toGregorianCalendar().getTime());
                    bean.setTargetName(targetArtifact.getName());
                    bean.setTargetUuid(targetUuid);
                    bean.setTargetType(targetArtifactType.getType());
                    indexedRelationships.addRelationship(bean);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                    // TODO handle the error case here?  what to do?
                }
            }
        }, artifact);
    }

}
