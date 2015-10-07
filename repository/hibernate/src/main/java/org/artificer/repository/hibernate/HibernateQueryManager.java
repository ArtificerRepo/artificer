package org.artificer.repository.hibernate;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.artificer.common.ArtificerException;
import org.artificer.common.query.ArtifactSummary;
import org.artificer.common.query.RelationshipType;
import org.artificer.common.query.ReverseRelationship;
import org.artificer.repository.QueryManager;
import org.artificer.repository.hibernate.query.HibernateQuery;
import org.artificer.repository.query.ArtificerQuery;
import org.artificer.repository.query.ArtificerQueryArgs;

/**
 * @author Brett Meyer
 */
public class HibernateQueryManager implements QueryManager {

    @Override
    public void login(String username, String password) {
        // not used
    }

    @Override
    public ArtificerQuery createQuery(String xpathTemplate, ArtificerQueryArgs args) {
        return new HibernateQuery(xpathTemplate, args);
    }

    @Override
    public ArtificerQuery createQuery(String xpathTemplate) throws ArtificerException {
        return createQuery(xpathTemplate, new ArtificerQueryArgs());
    }

    @Override
    public List<ReverseRelationship> reverseRelationships(final String uuid) throws ArtificerException {
                return new HibernateUtil.HibernateTask<List<ReverseRelationship>>() {
            @Override
            protected List<ReverseRelationship> doExecute(EntityManager entityManager) throws Exception {
                Query q = entityManager.createQuery(
                        "SELECT new org.artificer.common.query.ReverseRelationship(r.name, r.type, a1.uuid, a1.name, a1.model, a1.type)" +
                                " FROM ArtificerRelationship r" +
                                " INNER JOIN r.owner a1" +
                                " INNER JOIN r.targets t" +
                                " INNER JOIN t.target a2" +
                                " WHERE a1.trashed = false AND a2.uuid=:uuid");
                q.setParameter("uuid", uuid);
                q.unwrap(org.hibernate.Query.class).setCacheable(true);
                List<ReverseRelationship> reverseRelationships = q.getResultList();

                // If the artifact has derived artifacts, also need to include those...
                q = entityManager.createQuery(
                        "SELECT new org.artificer.common.query.ArtifactSummary(a.uuid, a.name, a.model, a.type)" +
                                " FROM ArtificerArtifact a" +
                                " INNER JOIN a.derivedFrom a1" +
                                " WHERE a1.trashed = false AND a1.uuid=:uuid");
                q.setParameter("uuid", uuid);
                q.unwrap(org.hibernate.Query.class).setCacheable(true);
                List<ArtifactSummary> derivedFroms = q.getResultList();
                for (ArtifactSummary derivedFrom : derivedFroms) {
                    reverseRelationships.add(new ReverseRelationship("relatedDocument", RelationshipType.DERIVED, derivedFrom));
                }

                // As well as expanded artifacts
                q = entityManager.createQuery(
                        "SELECT new org.artificer.common.query.ArtifactSummary(a.uuid, a.name, a.model, a.type)" +
                                " FROM ArtificerArtifact a" +
                                " INNER JOIN a.expandedFrom a1" +
                                " WHERE a1.trashed = false AND a1.uuid=:uuid");
                q.setParameter("uuid", uuid);
                List<ArtifactSummary> expandedFroms = q.getResultList();
                for (ArtifactSummary expandedFrom : expandedFroms) {
                    reverseRelationships.add(new ReverseRelationship("expandedFromArchive", RelationshipType.DERIVED, expandedFrom));
                }

                return reverseRelationships;
            }
        }.execute();
    }

    @Override
    public List<String> getTypes() throws ArtificerException {
        return new HibernateUtil.HibernateTask<List<String>>() {
            @Override
            protected List<String> doExecute(EntityManager entityManager) throws Exception {
                Query q = entityManager.createQuery("SELECT distinct type FROM ArtificerArtifact");

                q.unwrap(org.hibernate.Query.class).setCacheable(true);
                List<String> types = q.getResultList();

                return types;
            }
        }.execute();
    }
}
