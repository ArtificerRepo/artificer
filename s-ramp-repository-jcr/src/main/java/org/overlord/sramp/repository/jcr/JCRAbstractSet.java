/*
 * Copyright 2014 JBoss Inc
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

import org.overlord.sramp.repository.AbstractSet;

import javax.jcr.NodeIterator;
import javax.jcr.Session;

/**
 * @author Brett Meyer.
 */
public abstract class JCRAbstractSet implements AbstractSet {

    protected Session session;

    protected NodeIterator jcrNodes;

    protected int totalSize;

    public JCRAbstractSet(Session session, NodeIterator jcrNodes) throws Exception {
        this.session = session;
        this.jcrNodes = jcrNodes;

        // Do this here, rather than in #size().  If #pagedList is called, jcrNodes may be iterated through.
        totalSize = (int) jcrNodes.getSize();
    }

    @Override
    public int size() {
        return totalSize;
    }

    @Override
    public void close() {
        JCRRepositoryFactory.logoutQuietly(session);
    }
}
