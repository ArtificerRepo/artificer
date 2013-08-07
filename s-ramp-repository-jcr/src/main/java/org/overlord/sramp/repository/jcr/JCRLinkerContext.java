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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.jcr.Session;

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.overlord.sramp.common.SrampException;
import org.overlord.sramp.common.derived.LinkerContext;
import org.overlord.sramp.repository.jcr.query.JCRSrampQuery;
import org.overlord.sramp.repository.query.ArtifactSet;

/**
 * Linker context provided by the JCR implementation.
 * @author eric.wittmann@redhat.com
 */
public class JCRLinkerContext implements LinkerContext {

    private final Session session;

    /**
     * Constructor.
     * @param session
     */
    public JCRLinkerContext(Session session) {
        this.session = session;
    }

    /**
     * @see org.overlord.sramp.common.derived.LinkerContext#findArtifacts(java.lang.String, java.lang.String, java.util.Map)
     */
    @Override
    public Collection<BaseArtifactType> findArtifacts(String model, String type,
            Map<String, String> criteria) {
        StringBuilder builder = new StringBuilder();
        builder.append("/s-ramp/").append(model).append("/").append(type); //$NON-NLS-1$ //$NON-NLS-2$
        if (!criteria.isEmpty()) {
            builder.append("["); //$NON-NLS-1$
            boolean first = true;
            for (String key : criteria.keySet()) {
                String value = criteria.get(key);
                if (first) {
                    first = false;
                } else {
                    builder.append(" and "); //$NON-NLS-1$
                }
                builder.append("@").append(key.replace("'", "\\'")).append(" = '").append(value.replace("'", "\\'")).append("'"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
            }
            builder.append("]"); //$NON-NLS-1$
        }
        String xpath = builder.toString();
        JCRSrampQuery query = new JCRSrampQuery(xpath, "createdTimestamp", false); //$NON-NLS-1$
        query.setSession(session);
        try {
            ArtifactSet artifactSet = query.executeQuery();
            List<BaseArtifactType> artifacts = new ArrayList<BaseArtifactType>();
            for (BaseArtifactType artifact : artifactSet) {
                artifacts.add(artifact);
            }
            return artifacts;
        } catch (SrampException e) {
            throw new RuntimeException(e);
        }
    }

}
