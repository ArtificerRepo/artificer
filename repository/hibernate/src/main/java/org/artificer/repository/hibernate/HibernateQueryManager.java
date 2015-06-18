package org.artificer.repository.hibernate;

import org.artificer.common.ArtificerException;
import org.artificer.common.query.ArtifactSummary;
import org.artificer.common.query.RelationshipType;
import org.artificer.common.query.ReverseRelationship;
import org.artificer.repository.QueryManager;
import org.artificer.repository.hibernate.data.HibernateEntityToSrampVisitor;
import org.artificer.repository.hibernate.entity.ArtificerArtifact;
import org.artificer.repository.hibernate.query.HibernateQuery;
import org.artificer.repository.query.ArtificerQuery;
import org.artificer.repository.query.ArtificerQueryArgs;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

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
                        "SELECT new org.artificer.common.query.ReverseRelationship(r.name, r.type, a1.uuid, a1.name, a1.description, a1.model, a1.type, a1.derived)" +
                                " FROM ArtificerRelationship r" +
                                " INNER JOIN r.owner a1" +
                                " INNER JOIN r.targets t" +
                                " INNER JOIN t.target a2" +
                                " WHERE a1.trashed = false AND a2.uuid=:uuid");
                q.setParameter("uuid", uuid);
                List<ReverseRelationship> reverseRelationships = q.getResultList();

                // If the artifact has derived artifacts, also need to include those...
                q = entityManager.createQuery(
                        "SELECT new org.artificer.common.query.ArtifactSummary(a.uuid, a.name, a.description, a.model, a.type, a.derived)" +
                                " FROM ArtificerArtifact a" +
                                " INNER JOIN a.derivedFrom a1" +
                                " WHERE a1.trashed = false AND a1.uuid=:uuid");
                q.setParameter("uuid", uuid);
                List<ArtifactSummary> derivedFroms = q.getResultList();
                for (ArtifactSummary derivedFrom : derivedFroms) {
                    reverseRelationships.add(new ReverseRelationship("relatedDocument", RelationshipType.DERIVED, derivedFrom));
                }

                return reverseRelationships;
            }
        }.execute();
    }
}
