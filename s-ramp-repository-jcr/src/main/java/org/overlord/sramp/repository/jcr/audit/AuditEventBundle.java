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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFormatException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;

import org.overlord.sramp.repository.jcr.JCRConstants;

/**
 * Collection of events. Assists in analyzing a set of events as a group.
 *
 * @author eric.wittmann@redhat.com
 */
public class AuditEventBundle extends ArrayList<Event> {

    private static final long serialVersionUID = 7294693185326588856L;

    private final Map<String, Node> nodeCache = new HashMap<String, Node>();
    private final Session session;
    private String eventBundleType;

    /**
     * Constructor.
     *
     * @param events
     * @throws RepositoryException
     */
    public AuditEventBundle(Session session, EventIterator events) throws RepositoryException {
        this.session = session;
        this.eventBundleType = null;
        while (events.hasNext()) {
            Event event = events.nextEvent();
            this.eventBundleType = event.getUserData();
            if (isAuditing())
                continue;
            add(event);
        }
    }

    /**
     * Gets a node. Tries the cache first.
     *
     * @param jcrId
     * @throws ItemNotFoundException
     * @throws RepositoryException
     */
    public Node getNode(Event event) throws ItemNotFoundException, RepositoryException {
        return getNode(event.getIdentifier());
    }

    /**
     * Gets a node. Tries the cache first.
     *
     * @param jcrId
     * @throws ItemNotFoundException
     * @throws RepositoryException
     */
    public Node getNode(String jcrId) throws ItemNotFoundException, RepositoryException {
        Node node = nodeCache.get(jcrId);
        if (node == null) {
            node = session.getNodeByIdentifier(jcrId);
            nodeCache.put(jcrId, node);
        }
        return node;
    }

    /**
     * @return true if the set of events represents adding a new artifact
     * @throws RepositoryException
     * @throws ItemNotFoundException
     */
    public boolean isArtifactAdd() throws ItemNotFoundException, RepositoryException {
        return JCRAuditConstants.AUDIT_BUNDLE_ARTIFACT_ADDED_PHASE1.equals(this.getEventBundleType());
    }

    /**
     * @return true if the set of events represents adding deived artifacts
     * @throws ItemNotFoundException
     * @throws RepositoryException
     */
    public boolean isDerivedArtifactsAdded() throws ItemNotFoundException, RepositoryException {
        return JCRAuditConstants.AUDIT_BUNDLE_DERIVED_ARTIFACTS_ADDED_PHASE1.equals(this.getEventBundleType()) ||
                JCRAuditConstants.AUDIT_BUNDLE_DERIVED_ARTIFACTS_ADDED_PHASE2.equals(this.getEventBundleType());
    }

    /**
     * @return true if the set of events represents an update of an artifact
     * @throws RepositoryException
     * @throws ItemNotFoundException
     */
    public boolean isArtifactUpdate() throws ItemNotFoundException, RepositoryException {
        return JCRAuditConstants.AUDIT_BUNDLE_ARTIFACT_UPDATED.equals(this.getEventBundleType());
    }

    /**
     * Returns true if these events were caused by the auditing layer adding content to the JCR
     * repository.  We certainly don't want to audit our auditing.
     * @throws ValueFormatException
     * @throws PathNotFoundException
     * @throws RepositoryException
     */
    public boolean isAuditing() throws ValueFormatException, PathNotFoundException, RepositoryException {
        return JCRAuditConstants.AUDIT_BUNDLE_AUDITING.equals(this.getEventBundleType());
    }

    /**
     * For Artifact based events, returns the event representing the adding of the JCR node for the
     * artifact.  This will only work if isArtifactAdd() returned true.
     * @throws RepositoryException
     * @throws PathNotFoundException
     * @throws ValueFormatException
     */
    public Event getArtifactAddEvent() throws ValueFormatException, PathNotFoundException, RepositoryException {
        for (Event event : this) {
            if (event.getType() == Event.NODE_ADDED) {
                Node node = getNode(event);
                if (node.isNodeType(JCRConstants.SRAMP_BASE_ARTIFACT_TYPE) && !node.getProperty("sramp:derived").getBoolean()) {
                    return event;
                }
            }
        }
        return null;
    }

    /**
     * For derived artifact based events, returns a list of all of them.  An event bundle
     * may contain multiple derived-artifact-added events.
     * @throws RepositoryException
     * @throws ItemNotFoundException
     */
    public List<Event> getDerivedArtifactAddEvents() throws ItemNotFoundException, RepositoryException {
        List<Event> rval = new ArrayList<Event>();
        for (Event event : this) {
            if (event.getType() == Event.NODE_ADDED) {
                Node node = getNode(event);
                if (node.isNodeType(JCRConstants.SRAMP_BASE_ARTIFACT_TYPE) && node.getProperty("sramp:derived").getBoolean()) {
                    rval.add(event);
                }
            }
        }
        return rval;
    }

    /**
     * For Artifact based events, returns the event representing the adding of the JCR node for the
     * artifact.  This will only work if isArtifactAdd() returned true.
     * @throws RepositoryException
     * @throws ItemNotFoundException
     */
    public Event getArtifactUpdateEvent() throws ItemNotFoundException, RepositoryException {
        for (Event event : this) {
            if (event.getType() == Event.PROPERTY_ADDED || event.getType() == Event.PROPERTY_CHANGED || event.getType() == Event.PROPERTY_REMOVED) {
                Node node = getNode(event);
                if (node.isNodeType(JCRConstants.SRAMP_BASE_ARTIFACT_TYPE)) {
                    return event;
                }
            }
        }
        return null;
    }

    /**
     * @return the eventBundleType
     */
    public String getEventBundleType() {
        return eventBundleType;
    }

    /**
     * @return the JCR session
     */
    public Session getSession() {
        return session;
    }

}
