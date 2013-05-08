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
package org.overlord.sramp.repository.jcr.audit;

import java.util.Calendar;
import java.util.UUID;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.observation.Event;
import javax.jcr.version.VersionException;

import org.overlord.sramp.repository.jcr.JCRConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for all event bundle handlers.
 * @author eric.wittmann@redhat.com
 */
public abstract class AbstractAuditEventBundleHandler implements AuditEventBundleHandler {

    static Logger log = LoggerFactory.getLogger(AbstractAuditEventBundleHandler.class);

    /**
     * Creates a JCR node for the
     * @param artifactNode
     * @param who
     * @param when
     * @throws ValueFormatException
     * @throws VersionException
     * @throws LockException
     * @throws ConstraintViolationException
     * @throws RepositoryException
     */
    protected Node createAuditEntryNode(Node artifactNode, String type, String who, long when)
            throws ValueFormatException, VersionException, LockException, ConstraintViolationException,
            RepositoryException {
        String auditUuid = UUID.randomUUID().toString();
        Node auditEntryNode = artifactNode.addNode("audit:" + auditUuid, JCRConstants.SRAMP_AUDIT_ENTRY);
        long eventDate = when;
        Calendar eventCal = Calendar.getInstance();
        eventCal.setTimeInMillis(eventDate);

        auditEntryNode.setProperty("audit:uuid", auditUuid);
        auditEntryNode.setProperty("audit:sortId", eventDate);
        auditEntryNode.setProperty("audit:type", type);
        auditEntryNode.setProperty("audit:who", who);
        auditEntryNode.setProperty("audit:when", eventCal);

        return auditEntryNode;
    }

    /**
     * Creates the audit item node as a child of the given audit entry.
     * @param auditEntryNode
     * @param propertyAdded
     * @throws RepositoryException
     * @throws ConstraintViolationException
     * @throws LockException
     * @throws VersionException
     * @throws ValueFormatException
     */
    protected Node createAuditItemNode(Node auditEntryNode, String auditItemType) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        String auditItemNodeName = "audit:" + auditItemType.replace(':', '_');
        Node auditItemNode = auditEntryNode.addNode(auditItemNodeName, JCRConstants.SRAMP_AUDIT_ITEM);
        auditItemNode.setProperty("audit:type", auditItemType);
        return auditItemNode;
    }

    /**
     * Adds a property to the given audit item node.
     *
     * @param eventBundle
     * @param auditItemNode
     * @param event
     * @throws RepositoryException
     */
    protected void addPropertyToAuditItem(AuditEventBundle eventBundle, Node auditItemNode, Event event)
            throws RepositoryException {
        Node artifactNode = eventBundle.getNode(event);
        String path = event.getPath();
        String propertyName = path.substring(path.lastIndexOf('/') + 1);
        Property property = artifactNode.getProperty(propertyName);
        if (property.isMultiple()) {
            // TODO audit multi-value properties
            return;
        }
        String propertyValue = property.getString();
        auditItemNode.setProperty(propertyName, propertyValue);
    }

}
