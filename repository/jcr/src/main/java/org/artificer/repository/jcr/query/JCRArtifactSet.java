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
package org.artificer.repository.jcr.query;

import org.artificer.repository.jcr.JCRAbstractSet;
import org.artificer.repository.jcr.JCRNodeToArtifactFactory;
import org.artificer.repository.query.ArtifactSet;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A JCR implementation of an {@link org.artificer.repository.query.ArtifactSet}.
 *
 * @author eric.wittmann@redhat.com
 */
public class JCRArtifactSet extends JCRAbstractSet implements ArtifactSet, Iterator<BaseArtifactType> {

	private boolean logoutOnClose = true;

	/**
	 * Constructor.
	 * @param session
	 * @param jcrNodes
	 */
	public JCRArtifactSet(Session session, NodeIterator jcrNodes) throws Exception {
		super(session, jcrNodes);
	}

    /**
     * Constructor.
     * @param session
     * @param jcrNodes
     * @param logoutOnClose
     */
    public JCRArtifactSet(Session session, NodeIterator jcrNodes, boolean logoutOnClose) throws Exception {
        this(session, jcrNodes);
        this.logoutOnClose = logoutOnClose;
    }

	/**
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<BaseArtifactType> iterator() {
		return this;
	}

	@Override
	public void close() {
	    if (logoutOnClose)
	        super.close();
	}

	/**
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext() {
		return jcrNodes.hasNext();
	}

	/**
	 * @see java.util.Iterator#next()
	 */
	@Override
	public BaseArtifactType next() {
        Node jcrNode = jcrNodes.nextNode();
		return JCRNodeToArtifactFactory.createArtifact(this.session, jcrNode);
	}

	/**
	 * @see java.util.Iterator#remove()
	 */
	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

    @Override
    public List<BaseArtifactType> list() throws Exception {
        List<BaseArtifactType> artifacts = new ArrayList<BaseArtifactType>();
        while (hasNext()) {
            artifacts.add(next());
        }
        return artifacts;
    }

    @Override
    public List<BaseArtifactType> pagedList(long startIndex, long endIndex) throws Exception {
        // Get only the rows we're interested in.
        List<BaseArtifactType> artifacts = new ArrayList<BaseArtifactType>();
        int i = 0;
        while (hasNext()) {
           if (i >= startIndex && i <= endIndex) {
               artifacts.add(next());
            } else {
                // burn it
                jcrNodes.next();
            }
            i++;
        }

        return artifacts;
    }

}
