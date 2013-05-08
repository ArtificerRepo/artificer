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

import java.util.Iterator;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;

import org.jboss.downloads.overlord.sramp._2013.auditing.AuditEntry;
import org.overlord.sramp.repository.audit.AuditEntrySet;
import org.overlord.sramp.repository.jcr.JCRRepositoryFactory;
import org.overlord.sramp.repository.jcr.mapper.JCRNodeToAuditEntryFactory;

/**
 * JCR implementation of the {@link AuditEntrySet} interface.  This implementation iterates over
 * a set of JCR nodes.  Each node must be an audit:auditEntry JCR node.
 * @author eric.wittmann@redhat.com
 */
public class JCRAuditEntrySet implements AuditEntrySet, Iterator<AuditEntry> {

    private final Session session;
    private final NodeIterator jcrNodes;

    /**
     * Constructor.
     * @param session
     * @param jcrNodes
     */
    public JCRAuditEntrySet(Session session, NodeIterator jcrNodes) {
        this.session = session;
        this.jcrNodes = jcrNodes;
    }

    /**
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<AuditEntry> iterator() {
        return this;
    }

    /**
     * @see org.overlord.sramp.repository.audit.AuditEntrySet#size()
     */
    @Override
    public long size() {
        return this.jcrNodes.getSize();
    }

    /**
     * @see org.overlord.sramp.repository.audit.AuditEntrySet#close()
     */
    @Override
    public void close() {
        JCRRepositoryFactory.logoutQuietly(this.session);
    }

    /**
     * @see java.util.Iterator#hasNext()
     */
    @Override
    public boolean hasNext() {
        return this.jcrNodes.hasNext();
    }

    /**
     * @see java.util.Iterator#next()
     */
    @Override
    public AuditEntry next() {
        Node jcrNode = this.jcrNodes.nextNode();
        return JCRNodeToAuditEntryFactory.createAuditEntry(this.session, jcrNode);
    }

    /**
     * @see java.util.Iterator#remove()
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

}
