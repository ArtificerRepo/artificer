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
package org.artificer.repository.hibernate;

import org.artificer.common.ArtificerException;
import org.artificer.common.error.ArtificerNotFoundException;
import org.artificer.common.error.ArtificerServerException;
import org.artificer.repository.hibernate.entity.ArtificerArtifact;

import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Brett Meyer.
 */
public class HibernateRelationshipFactory {

    private final EntityManager entityManager;

    // Optimization.  When a new artifact is (derived and) stored, track the S-RAMP UUIDs
    // and entities here.  When creating the actual relationships, this saves *many* needless queries.
    private final Map<String, ArtificerArtifact> entities = new HashMap<>();

    /**
     * Constructor.
     * @param entityManager
     */
    public HibernateRelationshipFactory(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public ArtificerArtifact createRelationship(String uuid) throws ArtificerException {
        try {
            ArtificerArtifact entity;
            if (entities.containsKey(uuid)) {
                entity = entities.get(uuid);
            } else {
                entity  = HibernateUtil.getArtifact(uuid, entityManager, false);
            }

            if (entity == null) {
                throw ArtificerNotFoundException.artifactNotFound(uuid);
            }

            return entity;
        } catch (ArtificerException se) {
            throw se;
        } catch (Throwable t) {
            throw new ArtificerServerException(t);
        }
    }

    public void trackEntity(String uuid, ArtificerArtifact entity) {
        entities.put(uuid, entity);
    }
}
