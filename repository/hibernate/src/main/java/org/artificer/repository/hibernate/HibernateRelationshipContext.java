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
package org.artificer.repository.hibernate;

import org.artificer.common.query.ArtifactSummary;
import org.artificer.integration.artifactbuilder.RelationshipContext;
import org.artificer.repository.hibernate.query.HibernateQuery;

import javax.persistence.EntityManager;
import java.util.Collection;
import java.util.Map;

/**
 * RelationshipContext provided by the Hibernate repo implementation.
 *
 * @author Brett Meyer
 */
public class HibernateRelationshipContext implements RelationshipContext {

	private final EntityManager entityManager;

	public HibernateRelationshipContext(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

    /**
     * @see org.artificer.integration.artifactbuilder.RelationshipContext#findArtifacts(String, String, java.util.Map)
     */
    @Override
    public Collection<ArtifactSummary> findArtifacts(String model, String type,
            Map<String, String> criteria) throws Exception {
        StringBuilder builder = new StringBuilder("/s-ramp/" + model + "/" + type + "[");
        boolean first = true;
        for (String key : criteria.keySet()) {
            String value = criteria.get(key);

            if (!first) {
                builder.append(" and ");
            } else {
                first = false;
            }

            builder.append("@" + key + " = '" + value + "'");
        }
        builder.append("]");

        HibernateQuery query = new HibernateQuery(builder.toString(), entityManager);
        return query.executeQuery().getResults();
    }

}
