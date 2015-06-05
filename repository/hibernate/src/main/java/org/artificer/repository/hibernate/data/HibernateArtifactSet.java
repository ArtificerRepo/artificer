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
package org.artificer.repository.hibernate.data;

import org.artificer.repository.hibernate.entity.ArtificerArtifact;
import org.artificer.repository.query.ArtifactSet;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A JPA implementation of an {@link org.artificer.repository.query.ArtifactSet}.
 */
public class HibernateArtifactSet implements ArtifactSet, Iterator<BaseArtifactType> {

    private final Iterator<ArtificerArtifact> itr;
    private final int size;
    private final long totalSize;

    public HibernateArtifactSet(List<ArtificerArtifact> artificerArtifacts, long totalSize) throws Exception {
        itr = artificerArtifacts.iterator();
        size = artificerArtifacts.size();
        this.totalSize = totalSize;
    }

    /**
     * @see Iterable#iterator()
     */
    @Override
    public Iterator<BaseArtifactType> iterator() {
        return this;
    }

    /**
     * @see java.util.Iterator#next()
     */
    @Override
    public BaseArtifactType next() {
        try {
            ArtificerArtifact next = itr.next();
            return HibernateEntityToSrampVisitor.visit(next, false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
    public List<BaseArtifactType> list() throws Exception {
        List<BaseArtifactType> entries = new ArrayList<>();
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
