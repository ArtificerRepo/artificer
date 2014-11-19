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
package org.overlord.sramp.repository.jcr;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.Value;

import org.overlord.sramp.common.error.ArtifactNotFoundException;
import org.overlord.sramp.common.SrampException;
import org.overlord.sramp.common.error.SrampServerException;
import org.overlord.sramp.repository.jcr.mapper.ArtifactToJCRNodeVisitor.JCRReferenceFactory;
import org.overlord.sramp.repository.jcr.util.JCRUtils;

/**
 * An impl of a JCR reference factory.
 *
 * @author eric.wittmann@redhat.com
 */
public class JCRReferenceFactoryImpl implements JCRReferenceFactory {

    private Session session;

    /**
     * Constructor.
     * @param session
     */
    public JCRReferenceFactoryImpl(Session session) {
        this.session = session;
    }

    @Override
    public Value createReference(String uuid) throws SrampException {
        try {
            Node node = JCRUtils.findArtifactNodeByUuid(session, uuid);
            if (node == null) {
                throw new ArtifactNotFoundException(uuid);
            }
            return session.getValueFactory().createValue(node, true);
        } catch (SrampException se) {
            throw se;
        } catch (Throwable t) {
            throw new SrampServerException(t);
        }
    }

}
