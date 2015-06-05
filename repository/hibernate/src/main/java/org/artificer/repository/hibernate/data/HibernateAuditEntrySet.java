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
package org.artificer.repository.hibernate.data;

import org.artificer.repository.audit.AuditEntrySet;
import org.jboss.downloads.artificer._2013.auditing.AuditEntry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Brett Meyer
 */
public class HibernateAuditEntrySet implements AuditEntrySet, Iterator<AuditEntry> {

    private final Iterator<AuditEntry> itr;
    private final int size;
    private final long totalSize;

    public HibernateAuditEntrySet(List<AuditEntry> auditEntries, long totalSize) throws Exception {
        itr = auditEntries.iterator();
        size = auditEntries.size();
        this.totalSize = totalSize;
    }

    /**
     * @see Iterable#iterator()
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
        return itr.next();
    }

    @Override
    public boolean hasNext() {
        return itr.hasNext();
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
    public int size() {
        return size;
    }

    @Override
    public long totalSize() {
        return totalSize;
    }

    @Override
    public void close() {

    }

}
