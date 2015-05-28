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

import org.artificer.common.ArtificerConfig;
import org.artificer.common.ArtificerException;
import org.artificer.common.error.ArtificerNotFoundException;
import org.artificer.common.error.ArtificerServerException;
import org.artificer.repository.hibernate.entity.ArtificerArtifact;
import org.artificer.repository.hibernate.entity.ArtificerStoredQuery;
import org.hibernate.Hibernate;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.Query;
import java.util.Map;

/**
 * @author Brett Meyer.
 */
public class HibernateUtil {

    private static String persistenceUnit = "Artificer";

    private static EntityManagerFactory entityManagerFactory = null;

    public static abstract class HibernateTask<T> {
        public T execute() throws ArtificerException {
            EntityManager entityManager = null;
            try {
                entityManager = entityManager();
                entityManager.getTransaction().begin();

                T rtn = doExecute(entityManager);

                entityManager.getTransaction().commit();

                return rtn;
            } catch (ArtificerException ae) {
                if (entityManager != null) {
                    try {
                        entityManager.getTransaction().rollback();
                    } catch (Throwable t1) {
                        // eat it
                    }
                }
                throw ae;
            } catch (Throwable t) {
                if (entityManager != null) {
                    try {
                        entityManager.getTransaction().rollback();
                    } catch (Throwable t1) {
                        // eat it
                    }
                }
                throw new ArtificerServerException(t);
            } finally {
                if (entityManager != null) {
                    entityManager.close();
                }
            }
        }

        protected abstract T doExecute(EntityManager entityManager) throws Exception;
    }

    private static EntityManager entityManager() {
        if (entityManagerFactory == null) {
            // Pass in all hibernate.* settings from artificer.properties
            Map<String, Object> properties = ArtificerConfig.getConfigProperties("hibernate");
            entityManagerFactory = Persistence.createEntityManagerFactory(persistenceUnit, properties);
        }
        return entityManagerFactory.createEntityManager();
    }

    public static ArtificerArtifact getArtifact(String uuid, EntityManager entityManager, boolean fullFetch) throws ArtificerException {
        Query q = entityManager.createQuery("FROM ArtificerArtifact a WHERE a.trashed = false AND a.uuid = ?");
        q.setParameter(1, uuid);
        ArtificerArtifact artifact;
        try {
            artifact = (ArtificerArtifact) q.getSingleResult();
        } catch (NoResultException e) {
            throw ArtificerNotFoundException.artifactNotFound(uuid);
        }

        if (fullFetch) {
            Hibernate.initialize(artifact.getClassifiers());
            Hibernate.initialize(artifact.getComments());
            Hibernate.initialize(artifact.getNormalizedClassifiers());
            Hibernate.initialize(artifact.getRelationships());
        }

        return artifact;
    }

    public static ArtificerStoredQuery getStoredQuery(String queryName, EntityManager entityManager) throws ArtificerException {
        ArtificerStoredQuery storedQuery = entityManager.find(ArtificerStoredQuery.class, queryName);
        if (storedQuery == null) {
            throw ArtificerNotFoundException.storedQueryNotFound(queryName);
        }
        return storedQuery;
    }

    public static void setPersistenceUnit(String name) {
        persistenceUnit = name;
    }
}
