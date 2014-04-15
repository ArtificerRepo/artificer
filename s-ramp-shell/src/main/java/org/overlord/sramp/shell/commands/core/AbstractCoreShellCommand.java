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
package org.overlord.sramp.shell.commands.core;

import javax.xml.namespace.QName;

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.overlord.sramp.atom.err.SrampAtomException;
import org.overlord.sramp.client.SrampClientException;
import org.overlord.sramp.client.query.ArtifactSummary;
import org.overlord.sramp.client.query.QueryResultSet;
import org.overlord.sramp.shell.BuiltInShellCommand;
import org.overlord.sramp.shell.api.InvalidCommandArgumentException;
import org.overlord.sramp.shell.i18n.Messages;

/**
 * Abstract class with common methods and attributes for all the s-ramp core
 * shell commands.
 *
 * @author David Virgil Naranjo
 */
public abstract class AbstractCoreShellCommand extends BuiltInShellCommand {

    /**
     * Gets the artifact.
     *
     * @param feedIndex
     *            the feed index
     * @param id
     *            the id
     * @return the artifact
     * @throws InvalidCommandArgumentException
     *             the invalid command argument exception
     */
    protected BaseArtifactType getArtifact(Integer feedIndex, String id)
            throws InvalidCommandArgumentException {
        QName feedVarName = new QName("s-ramp", "feed"); //$NON-NLS-1$ //$NON-NLS-2$
        BaseArtifactType artifact;
        try {
            if (feedIndex != null) { //$NON-NLS-1$
                int feedIdx = feedIndex - 1;
                QueryResultSet rset = (QueryResultSet) getContext().getVariable(feedVarName);
                if (feedIdx < 0 || feedIdx >= rset.size()) {
                    throw new InvalidCommandArgumentException(Messages.i18n.format("FeedIndexOutOfRange")); //$NON-NLS-1$
                }
                ArtifactSummary summary = rset.get(feedIdx);
                String artifactUUID = summary.getUuid();
                artifact = client.getArtifactMetaData(summary.getType(), artifactUUID);
            } else { //$NON-NLS-1$
                artifact = client.getArtifactMetaData(id);
            }
        } catch (SrampAtomException e) {
            throw new InvalidCommandArgumentException(Messages.i18n.format("MissingSRAMPConnection"));
        } catch (SrampClientException e2) {
            throw new InvalidCommandArgumentException(Messages.i18n.format("MissingSRAMPConnection"));
        }
        return artifact;
    }





    /**
     * Gets the artifact.
     *
     * @return the artifact
     */
    protected BaseArtifactType getArtifact() {
        QName artifactVarName = new QName("s-ramp", "artifact"); //$NON-NLS-1$ //$NON-NLS-2$
        BaseArtifactType artifact = (BaseArtifactType) getContext().getVariable(artifactVarName);
        return artifact;
    }
}
