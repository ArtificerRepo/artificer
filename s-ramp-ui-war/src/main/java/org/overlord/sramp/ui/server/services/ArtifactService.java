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

import javax.inject.Inject;

import org.jboss.errai.bus.server.annotations.Service;
import org.overlord.sramp.atom.err.SrampAtomException;
import org.overlord.sramp.client.SrampClientException;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.ui.client.shared.beans.ArtifactBean;
import org.overlord.sramp.ui.client.shared.exceptions.SrampUiException;
import org.overlord.sramp.ui.client.shared.services.IArtifactService;
import org.overlord.sramp.ui.server.api.SrampApiClientAccessor;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.Property;

/**
 * Concrete implementation of the artifact service.
 *
 * @author eric.wittmann@redhat.com
 */
@Service
public class ArtifactService implements IArtifactService {

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
            bean.setCreatedBy(artifact.getCreatedBy());
            bean.setCreatedOn(artifact.getCreatedTimestamp().toGregorianCalendar().getTime());
            bean.setUpdatedOn(artifact.getLastModifiedTimestamp().toGregorianCalendar().getTime());
            bean.setUpdatedBy(artifact.getLastModifiedBy());
            bean.setDerived(artifactType.getArtifactType().isDerived());
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
     * @see org.overlord.sramp.ui.client.shared.services.IArtifactService#update(org.overlord.sramp.ui.client.shared.beans.ArtifactBean)
     */
    @Override
    public void update(ArtifactBean artifact) throws SrampUiException {
        // TODO implement this!
        throw new SrampUiException("Not yet implemented.");
    }

}
