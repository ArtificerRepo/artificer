package org.artificer.repository.hibernate;

import org.apache.commons.lang.StringUtils;
import org.artificer.common.ArtificerException;
import org.artificer.common.error.ArtificerServerException;
import org.artificer.common.error.ArtificerUserException;
import org.artificer.repository.AuditManager;
import org.artificer.repository.hibernate.audit.ArtificerAuditEntry;
import org.artificer.repository.hibernate.audit.HibernateAuditor;
import org.artificer.repository.hibernate.entity.ArtificerArtifact;
import org.artificer.repository.query.ArtificerQueryArgs;
import org.artificer.repository.query.PagedResult;
import org.jboss.downloads.artificer._2013.auditing.AuditEntry;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.List;
import java.util.UUID;

/**
 * @author Brett Meyer
 */
public class HibernateAuditManager implements AuditManager {

    @Override
    public void login(String username, String password) {
        // not used
    }

    @Override
    public AuditEntry addAuditEntry(final String artifactUuid, final AuditEntry srampAuditEntry)
            throws ArtificerException {
        return new HibernateUtil.HibernateTask<AuditEntry>() {
            @Override
            protected AuditEntry doExecute(EntityManager entityManager) throws Exception {
                if (StringUtils.isBlank(srampAuditEntry.getUuid())) {
                    srampAuditEntry.setUuid(UUID.randomUUID().toString());
                }

                ArtificerArtifact artifact = HibernateUtil.getArtifact(artifactUuid, entityManager, false);
                entityManager.persist(HibernateAuditor.auditEntry(srampAuditEntry, artifact));

                return srampAuditEntry;
            }
        }.execute();
    }

    @Override
    public AuditEntry getArtifactAuditEntry(final String artifactUuid, final String auditEntryUuid) throws ArtificerException {
        return new HibernateUtil.HibernateTask<AuditEntry>() {
            @Override
            protected AuditEntry doExecute(EntityManager entityManager) throws Exception {
                Query q = entityManager.createQuery(
                        "SELECT au FROM ArtificerAuditEntry au WHERE au.uuid=:uuid ORDER BY au.id DESC");
                q.setParameter("uuid", auditEntryUuid);
                q.unwrap(org.hibernate.Query.class).setCacheable(true);
                try {
                    ArtificerAuditEntry auditEntry = (ArtificerAuditEntry) q.getSingleResult();
                    return HibernateAuditor.auditEntry(auditEntry);
                } catch (NoResultException e) {
                    throw ArtificerUserException.auditEntryNotFound(artifactUuid, auditEntryUuid);
                }
            }
        }.execute();
    }

    @Override
    public PagedResult<AuditEntry> getArtifactAuditEntries(final String artifactUuid, final ArtificerQueryArgs args) throws ArtificerException {
        try {
            return new HibernateUtil.HibernateTask<PagedResult<AuditEntry>>() {
                @Override
                protected PagedResult<AuditEntry> doExecute(EntityManager entityManager) throws Exception {
                    Query q = entityManager.createQuery(
                            "SELECT au FROM ArtificerAuditEntry au INNER JOIN au.artifact a WHERE a.uuid=:uuid ORDER BY au.id DESC");
                    q.setParameter("uuid", artifactUuid);
                    q.unwrap(org.hibernate.Query.class).setCacheable(true);
                    args.applyPaging(q);
                    List<ArtificerAuditEntry> auditEntries = q.getResultList();

                    q = entityManager.createQuery(
                            "SELECT count(au) FROM ArtificerAuditEntry au INNER JOIN au.artifact a WHERE a.uuid=:uuid");
                    q.setParameter("uuid", artifactUuid);
                    q.unwrap(org.hibernate.Query.class).setCacheable(true);
                    args.applyPaging(q);
                    long totalSize = (Long) q.getSingleResult();

                    return new PagedResult<>(HibernateAuditor.auditEntries(auditEntries), "", totalSize, args);
                }
            }.execute();
        } catch (ArtificerException ae) {
            throw ae;
        } catch (Throwable t) {
            throw new ArtificerServerException(t);
        }
    }

    @Override
    public PagedResult<AuditEntry> getArtifactAuditEntries(final String artifactUuid) throws ArtificerException {
        return getArtifactAuditEntries(artifactUuid, new ArtificerQueryArgs());
    }

    @Override
    public PagedResult<AuditEntry> getUserAuditEntries(final String username, final ArtificerQueryArgs args) throws ArtificerException {
        try {
            return new HibernateUtil.HibernateTask<PagedResult<AuditEntry>>() {
                @Override
                protected PagedResult<AuditEntry> doExecute(EntityManager entityManager) throws Exception {
                    Query q = entityManager.createQuery(
                            "SELECT au FROM ArtificerAuditEntry au WHERE au.modifiedBy.username=:username ORDER BY au.id DESC");
                    q.setParameter("username", username);
                    q.unwrap(org.hibernate.Query.class).setCacheable(true);
                    args.applyPaging(q);
                    List<ArtificerAuditEntry> auditEntries = q.getResultList();

                    q = entityManager.createQuery(
                            "SELECT count(au) FROM ArtificerAuditEntry au WHERE au.modifiedBy.username=:username");
                    q.setParameter("username", username);
                    q.unwrap(org.hibernate.Query.class).setCacheable(true);
                    args.applyPaging(q);
                    long totalSize = (Long) q.getSingleResult();

                    return new PagedResult<>(HibernateAuditor.auditEntries(auditEntries), "", totalSize, args);
                }
            }.execute();
        } catch (ArtificerException ae) {
            throw ae;
        } catch (Throwable t) {
            throw new ArtificerServerException(t);
        }
    }

    @Override
    public PagedResult<AuditEntry> getUserAuditEntries(final String username) throws ArtificerException {
        return getUserAuditEntries(username, new ArtificerQueryArgs());
    }
}
