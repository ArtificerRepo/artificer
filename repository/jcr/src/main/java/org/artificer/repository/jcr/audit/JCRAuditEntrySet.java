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
package org.artificer.repository.jcr.audit;

import org.artificer.repository.audit.AuditEntrySet;
import org.artificer.repository.jcr.mapper.JCRNodeToAuditEntryFactory;
import org.jboss.downloads.artificer._2013.auditing.AuditEntry;
import org.artificer.repository.jcr.JCRAbstractSet;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * JCR implementation of the {@link org.artificer.repository.audit.AuditEntrySet} interface.  This implementation iterates over
 * a set of JCR nodes.  Each node must be an audit:auditEntry JCR node.
 * @author eric.wittmann@redhat.com
 */
public class JCRAuditEntrySet extends JCRAbstractSet implements AuditEntrySet, Iterator<AuditEntry> {

    /**
     * Constructor.
     * @param session
     * @param jcrNodes
     */
    public JCRAuditEntrySet(Session session, NodeIterator jcrNodes) throws Exception {
        super(session, jcrNodes);
    }

    /**
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<AuditEntry> iterator() {
        return this;
    }

    /**
     * @see java.util.Iterator#next()
     */
    @Override
    public AuditEntry next() {
        Node jcrNode = jcrNodes.nextNode();
        return JCRNodeToAuditEntryFactory.createAuditEntry(session, jcrNode);
    }

    @Override
    public boolean hasNext() {
        return jcrNodes.hasNext();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<AuditEntry> list() throws Exception {
        List<AuditEntry> entries = new ArrayList<AuditEntry>();
        while (hasNext()) {
            entries.add(next());
        }
        return entries;
    }

    @Override
    public List<AuditEntry> pagedList(long startIndex, long endIndex) throws Exception {
        // Get only the rows we're interested in.
        List<AuditEntry> entries = new ArrayList<AuditEntry>();
        int i = 0;
        while (hasNext()) {
            if (i >= startIndex && i <= endIndex) {
                entries.add(next());
            } else {
                // burn it
                jcrNodes.next();
            }
            i++;
        }

        return entries;
    }

}
