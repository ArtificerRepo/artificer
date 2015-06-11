package org.artificer.repository.hibernate;

import org.artificer.common.ArtificerException;
import org.artificer.common.ReverseRelationship;
import org.artificer.repository.QueryManager;
import org.artificer.repository.hibernate.data.HibernateEntityToSrampVisitor;
import org.artificer.repository.hibernate.entity.ArtificerArtifact;
import org.artificer.repository.hibernate.entity.ArtificerRelationship;
import org.artificer.repository.hibernate.entity.ArtificerRelationshipType;
import org.artificer.repository.hibernate.query.HibernateQuery;
import org.artificer.repository.query.ArtificerQuery;
import org.artificer.repository.query.ArtificerQueryArgs;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
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
                List<ReverseRelationship> rval = new ArrayList<ReverseRelationship>();

                Query q = entityManager.createQuery(
                        "SELECT r FROM ArtificerRelationship r" +
                                " INNER JOIN r.owner a1" +
                                " INNER JOIN r.targets t" +
                                " INNER JOIN t.target a2" +
                                " WHERE a1.trashed = false AND a2.uuid=:uuid");
                q.setParameter("uuid", uuid);
                List<ArtificerRelationship> reverseRelationships = q.getResultList();
                for (ArtificerRelationship reverseRelationship : reverseRelationships) {
                    BaseArtifactType relationshipOwner = HibernateEntityToSrampVisitor.visit(
                            reverseRelationship.getOwner(), false);
                    rval.add(new ReverseRelationship(reverseRelationship.getName(),
                            relationshipOwner, reverseRelationship.getType().equals(ArtificerRelationshipType.GENERIC)));
                }

                // If the artifact has derived artifacts, also need to include those...
                q = entityManager.createQuery(
                        "SELECT a FROM ArtificerArtifact a" +
                                " INNER JOIN a.derivedFrom a1" +
                                " WHERE a1.trashed = false AND a1.uuid=:uuid");
                q.setParameter("uuid", uuid);
                List<ArtificerArtifact> derivedFroms = q.getResultList();
                for (ArtificerArtifact derivedFrom : derivedFroms) {
                    BaseArtifactType relationshipOwner = HibernateEntityToSrampVisitor.visit(derivedFrom, false);
                    rval.add(new ReverseRelationship("relatedDocument", relationshipOwner, false));
                }

                return rval;
            }
        }.execute();
    }
}
