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
package org.overlord.sramp.shell.commands.core;

import java.util.List;

import javax.xml.namespace.QName;

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.overlord.sramp.client.SrampAtomApiClient;
import org.overlord.sramp.client.query.ArtifactSummary;
import org.overlord.sramp.client.query.QueryResultSet;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.shell.BuiltInShellCommand;
import org.overlord.sramp.shell.api.InvalidCommandArgumentException;
import org.overlord.sramp.shell.i18n.Messages;
import org.overlord.sramp.shell.util.FileNameCompleter;

/**
 * Deletes an artifact from the S-RAMP repository.
 *
 * @author eric.wittmann@redhat.com
 */
public class DeleteCommand extends BuiltInShellCommand {

	/**
	 * Constructor.
	 */
	public DeleteCommand() {
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.ShellCommand#execute()
	 */
	@Override
	public boolean execute() throws Exception {
		QName clientVarName = new QName("s-ramp", "client"); //$NON-NLS-1$ //$NON-NLS-2$
		QName artifactVarName = new QName("s-ramp", "artifact"); //$NON-NLS-1$ //$NON-NLS-2$
        QName feedVarName = new QName("s-ramp", "feed"); //$NON-NLS-1$ //$NON-NLS-2$

		SrampAtomApiClient client = (SrampAtomApiClient) getContext().getVariable(clientVarName);
		if (client == null) {
			print(Messages.i18n.format("MissingSRAMPConnection")); //$NON-NLS-1$
			return false;
		}

        BaseArtifactType artifact = null;
		String artifactIdArg = this.optionalArgument(0);
        if (artifactIdArg == null) {
            artifact = (BaseArtifactType) getContext().getVariable(artifactVarName);
            if (artifact == null) {
                print(Messages.i18n.format("NoActiveArtifact")); //$NON-NLS-1$
                return false;
            }
        } else {
    		String idType = artifactIdArg.substring(0, artifactIdArg.indexOf(':'));
    		if ("feed".equals(idType)) { //$NON-NLS-1$
    		    QueryResultSet rset = (QueryResultSet) getContext().getVariable(feedVarName);
    		    int feedIdx = Integer.parseInt(artifactIdArg.substring(artifactIdArg.indexOf(':')+1)) - 1;
    		    if (feedIdx < 0 || feedIdx >= rset.size()) {
    		        throw new InvalidCommandArgumentException(0, Messages.i18n.format("FeedIndexOutOfRange")); //$NON-NLS-1$
    		    }
    		    ArtifactSummary summary = rset.get(feedIdx);
    		    String artifactUUID = summary.getUuid();
    		    artifact = client.getArtifactMetaData(summary.getType(), artifactUUID);
    		} else if ("uuid".equals(idType)) { //$NON-NLS-1$
                String artifactUUID = artifactIdArg.substring(artifactIdArg.indexOf(':') + 1);
                artifact = client.getArtifactMetaData(artifactUUID);
    		} else {
    		    throw new InvalidCommandArgumentException(0, Messages.i18n.format("InvalidArtifactIdFormat")); //$NON-NLS-1$
    		}
		}

		try {
			client.deleteArtifact(artifact.getUuid(), ArtifactType.valueOf(artifact));
			print(Messages.i18n.format("Delete.Success", artifact.getName())); //$NON-NLS-1$
		} catch (Exception e) {
			print(Messages.i18n.format("Delete.Failure")); //$NON-NLS-1$
			print("\t" + e.getMessage()); //$NON-NLS-1$
	        return false;
		}
        return true;
	}

    /**
     * @see org.overlord.sramp.shell.api.shell.AbstractShellCommand#tabCompletion(java.lang.String, java.util.List)
     */
    @Override
    public int tabCompletion(String lastArgument, List<CharSequence> candidates) {
        if (getArguments().isEmpty() && (lastArgument == null || "feed:".startsWith(lastArgument))) { //$NON-NLS-1$
            QName feedVarName = new QName("s-ramp", "feed"); //$NON-NLS-1$ //$NON-NLS-2$
            QueryResultSet rset = (QueryResultSet) getContext().getVariable(feedVarName);
            if (rset != null) {
                for (int idx = 0; idx < rset.size(); idx++) {
                    String candidate = "feed:" + (idx+1); //$NON-NLS-1$
                    if (lastArgument == null) {
                        candidates.add(candidate);
                    }
                    if (lastArgument != null && candidate.startsWith(lastArgument)) {
                        candidates.add(candidate);
                    }
                }
            }
            return 0;
        } else if (getArguments().size() == 1) {
            if (lastArgument == null)
                lastArgument = ""; //$NON-NLS-1$
            FileNameCompleter delegate = new FileNameCompleter();
            return delegate.complete(lastArgument, lastArgument.length(), candidates);
        } else {
            return -1;
        }
    }

}
