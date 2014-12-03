/*
 * Copyright 2012 JBoss Inc
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
package org.overlord.sramp.repository.jcr;

import org.jboss.downloads.overlord.sramp._2013.auditing.AuditEntry;
import org.jboss.downloads.overlord.sramp._2013.auditing.AuditItemType;
import org.jboss.downloads.overlord.sramp._2013.auditing.AuditItemType.Property;
import org.overlord.sramp.common.ArtifactNotFoundException;
import org.overlord.sramp.common.AuditEntryNotFoundException;
import org.overlord.sramp.common.SrampException;
import org.overlord.sramp.common.SrampServerException;
import org.overlord.sramp.repository.AuditManager;
import org.overlord.sramp.repository.audit.AuditEntrySet;
import org.overlord.sramp.repository.jcr.audit.JCRAuditEntrySet;
import org.overlord.sramp.repository.jcr.i18n.Messages;
import org.overlord.sramp.repository.jcr.mapper.JCRNodeToAuditEntryFactory;
import org.overlord.sramp.repository.jcr.util.JCRUtils;
import org.overlord.sramp.repository.query.InvalidQueryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.query.QueryResult;
import java.util.List;
import java.util.UUID;

/**
 * An implementation of the {@link AuditManager} using JCR.  Works along with the
 * JCR PersistenceManager implementation ({@link JCRPersistence}).
 *
 * @author eric.wittmann@redhat.com
 */

public class JCRAuditManager implements AuditManager {

    private static Logger log = LoggerFactory.getLogger(JCRAuditManager.class);
    private static final String AUDIT_ENTRY_QUERY = "SELECT auditEntry.*"
            + " FROM [sramp:baseArtifactType] AS artifact"
            + " JOIN [audit:auditEntry] AS auditEntry ON ISCHILDNODE(auditEntry, artifact) "
            + "WHERE artifact.[sramp:uuid] = '%1$s' AND auditEntry.[audit:uuid] = '%2$s'";
    private static final String ARTIFACT_AUDIT_TRAIL_QUERY = "SELECT auditEntry.*"
            + " FROM [sramp:baseArtifactType] AS artifact"
            + " JOIN [audit:auditEntry] AS auditEntry ON ISCHILDNODE(auditEntry, artifact) "
            + "WHERE artifact.[sramp:uuid] = '%1$s' ORDER BY auditEntry.[audit:sortId] DESC";
    private static final String USER_AUDIT_TRAIL_QUERY = "SELECT auditEntry.*"
            + " FROM [audit:auditEntry] AS auditEntry "
            + "WHERE auditEntry.[jcr:createdBy] = '%1$s' ORDER BY auditEntry.[audit:sortId] DESC";

	/**
	 * Default constructor.
	 */
	public JCRAuditManager() {
	}

    /**
     * @see org.overlord.sramp.repository.AuditManager#addAuditEntry(java.lang.String, org.jboss.downloads.overlord.sramp._2013.auditing.AuditEntry)
     */
    @Override
    public AuditEntry addAuditEntry(String artifactUuid, AuditEntry entry) throws SrampException {
        Session session = null;
        try {
            session = JCRRepositoryFactory.getSession();
            Node artifactNode = JCRUtils.findArtifactNodeByUuid(session, artifactUuid);
            if (artifactNode != null) {
                String auditEntryUuid = UUID.randomUUID().toString();
                Node auditEntryNode = artifactNode.addNode("audit:" + auditEntryUuid, JCRConstants.SRAMP_AUDIT_ENTRY);
                entry.setUuid(auditEntryUuid);

                auditEntryNode.setProperty("audit:uuid", entry.getUuid());
                auditEntryNode.setProperty("audit:sortId", System.currentTimeMillis());
                auditEntryNode.setProperty("audit:type", entry.getType());

                List<AuditItemType> auditItems = entry.getAuditItem();
                for (AuditItemType auditItem : auditItems) {
                    String type = auditItem.getType();
                    String auditItemNodeName = "audit:" + type.replace(':', '_');
                    Node auditItemNode = auditEntryNode.addNode(auditItemNodeName, JCRConstants.SRAMP_AUDIT_ITEM);
                    auditItemNode.setProperty("audit:type", type);
                    List<Property> properties = auditItem.getProperty();
                    for (Property property : properties) {
                        String propertyName = property.getName();
                        String propertyValue = property.getValue();
                        auditItemNode.setProperty(propertyName, propertyValue);
                    }
                }

                session.save();
                return entry;
            } else {
                throw new ArtifactNotFoundException(artifactUuid);
            }
        } catch (SrampException se) {
            throw se;
        } catch (Throwable t) {
            throw new SrampServerException(t);
        } finally {
            JCRRepositoryFactory.logoutQuietly(session);
        }
    }

    /**
     * @see org.overlord.sramp.repository.AuditManager#getArtifactAuditEntry(java.lang.String, java.lang.String)
     */
    @Override
    public AuditEntry getArtifactAuditEntry(String artifactUuid, String auditEntryUuid) throws SrampException {
        // Prevent injection.
        if (artifactUuid.indexOf('\'') >= 0 || auditEntryUuid.indexOf('\'') >= 0)
            throw new InvalidQueryException();
        String jcrSql2Query = String.format(AUDIT_ENTRY_QUERY, artifactUuid, auditEntryUuid);
        Session session = null;
        try {
            session = JCRRepositoryFactory.getSession();
            javax.jcr.query.QueryManager jcrQueryManager = session.getWorkspace().getQueryManager();
            javax.jcr.query.Query jcrQuery = jcrQueryManager.createQuery(jcrSql2Query, JCRConstants.JCR_SQL2);
            long startTime = System.currentTimeMillis();
            QueryResult jcrQueryResult = jcrQuery.execute();
            NodeIterator jcrNodes = jcrQueryResult.getNodes();
            long endTime = System.currentTimeMillis();
            log.debug(Messages.i18n.format("QUERY_EXECUTED", jcrSql2Query));
            log.debug(Messages.i18n.format("QUERY_EXECUTED_IN", endTime - startTime));
            if (jcrNodes.getSize() == 1) {
                Node node = jcrNodes.nextNode();
                return JCRNodeToAuditEntryFactory.createAuditEntry(session, node);
            } else {
                throw new AuditEntryNotFoundException(artifactUuid, auditEntryUuid);
            }
        } catch (Throwable t) {
            JCRRepositoryFactory.logoutQuietly(session);
            throw new SrampServerException(t);
        }
    }

    /**
     * @see org.overlord.sramp.repository.AuditManager#getArtifactAuditEntries(java.lang.String)
     */
    @Override
    public AuditEntrySet getArtifactAuditEntries(String artifactUuid) throws SrampException {
        // Prevent injection.
        if (artifactUuid.indexOf('\'') >= 0)
            throw new InvalidQueryException();
        String jcrSql2Query = String.format(ARTIFACT_AUDIT_TRAIL_QUERY, artifactUuid);
        return doAuditQuery(jcrSql2Query);
    }

    /**
     * @see org.overlord.sramp.repository.AuditManager#getUserAuditEntries(java.lang.String)
     */
    @Override
    public AuditEntrySet getUserAuditEntries(String username) throws SrampException {
        // Prevent injection.
        if (username.indexOf('\'') >= 0)
            throw new InvalidQueryException();
        String jcrSql2Query = String.format(USER_AUDIT_TRAIL_QUERY, username);
        return doAuditQuery(jcrSql2Query);
    }

    /**
     * Performs the audit query and returns the result as an audit entry set.
     * @param query
     * @throws SrampServerException
     */
    private AuditEntrySet doAuditQuery(String query) throws SrampServerException {
        Session session = null;
        try {
            session = JCRRepositoryFactory.getSession();
            javax.jcr.query.QueryManager jcrQueryManager = session.getWorkspace().getQueryManager();
            javax.jcr.query.Query jcrQuery = jcrQueryManager.createQuery(query, JCRConstants.JCR_SQL2);
            long startTime = System.currentTimeMillis();
            QueryResult jcrQueryResult = jcrQuery.execute();
            NodeIterator jcrNodes = jcrQueryResult.getNodes();
            long endTime = System.currentTimeMillis();
            log.debug(Messages.i18n.format("QUERY_EXECUTED", query));
            log.debug(Messages.i18n.format("QUERY_EXECUTED_IN", endTime - startTime));
            return new JCRAuditEntrySet(session, jcrNodes);
        } catch (Throwable t) {
            JCRRepositoryFactory.logoutQuietly(session);
            throw new SrampServerException(t);
        }
    }

}
