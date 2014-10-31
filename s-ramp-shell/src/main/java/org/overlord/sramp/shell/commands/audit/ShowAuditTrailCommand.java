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
package org.overlord.sramp.shell.commands.audit;

import java.util.List;

import javax.xml.namespace.QName;

import org.overlord.sramp.client.SrampAtomApiClient;
import org.overlord.sramp.client.audit.AuditEntrySummary;
import org.overlord.sramp.client.audit.AuditResultSet;
import org.overlord.sramp.client.query.ArtifactSummary;
import org.overlord.sramp.client.query.QueryResultSet;
import org.overlord.sramp.shell.BuiltInShellCommand;
import org.overlord.sramp.shell.api.InvalidCommandArgumentException;
import org.overlord.sramp.shell.commands.core.FeedTabCompleter;
import org.overlord.sramp.shell.i18n.Messages;

/**
 * Displays the audit trail for an artifact.
 *
 * @author eric.wittmann@redhat.com
 */
public class ShowAuditTrailCommand extends BuiltInShellCommand {

	/**
	 * Constructor.
	 */
	public ShowAuditTrailCommand() {
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.ShellCommand#execute()
	 */
	@Override
	public boolean execute() throws Exception {
        String artifactIdArg = this.requiredArgument(0, Messages.i18n.format("AuditTrail.InvalidArgMsg.ArtifactId")); //$NON-NLS-1$
        if (!artifactIdArg.contains(":")) { //$NON-NLS-1$
            throw new InvalidCommandArgumentException(0, Messages.i18n.format("InvalidArtifactIdFormat")); //$NON-NLS-1$
        }
        QName clientVarName = new QName("s-ramp", "client"); //$NON-NLS-1$ //$NON-NLS-2$
        QName feedVarName = new QName("s-ramp", "feed"); //$NON-NLS-1$ //$NON-NLS-2$
        SrampAtomApiClient client = (SrampAtomApiClient) getContext().getVariable(clientVarName);
        if (client == null) {
            print(Messages.i18n.format("MissingSRAMPConnection")); //$NON-NLS-1$
            return false;
        }

        String artifactUuid = null;
        String idType = artifactIdArg.substring(0, artifactIdArg.indexOf(':'));
        if ("feed".equals(idType)) { //$NON-NLS-1$
            QueryResultSet rset = (QueryResultSet) getContext().getVariable(feedVarName);
            if (rset == null) {
                throw new InvalidCommandArgumentException(0, Messages.i18n.format("NoFeed")); //$NON-NLS-1$
            }
            int feedIdx = Integer.parseInt(artifactIdArg.substring(artifactIdArg.indexOf(':')+1)) - 1;
            if (feedIdx < 0 || feedIdx >= rset.size()) {
                throw new InvalidCommandArgumentException(0, Messages.i18n.format("FeedIndexOutOfRange")); //$NON-NLS-1$
            }
            ArtifactSummary summary = rset.get(feedIdx);
            artifactUuid = summary.getUuid();
        } else if ("uuid".equals(idType)) { //$NON-NLS-1$
            artifactUuid = artifactIdArg.substring(artifactIdArg.indexOf(':') + 1);
        } else {
            throw new InvalidCommandArgumentException(0, Messages.i18n.format("InvalidIdFormat")); //$NON-NLS-1$
        }

        AuditResultSet auditTrail = client.getAuditTrailForArtifact(artifactUuid);
        QName artifactVarName = new QName("audit", "auditTrail"); //$NON-NLS-1$ //$NON-NLS-2$
        getContext().setVariable(artifactVarName, auditTrail);
        print(Messages.i18n.format("ShowAuditTrail.EntriesSummary", auditTrail.size())); //$NON-NLS-1$
        print("  Idx  " + Messages.i18n.format("AuditEntryLabel")); //$NON-NLS-1$ //$NON-NLS-2$
        print("  ---  -----------"); //$NON-NLS-1$
        int idx = 1;
        for (AuditEntrySummary auditEntrySummary : auditTrail) {
            print("  %1$3d  %2$s", idx++, auditEntrySummary.toString()); //$NON-NLS-1$
        }
        return true;
	}

    /**
     * @see org.overlord.sramp.shell.api.shell.AbstractShellCommand#tabCompletion(java.lang.String, java.util.List)
     */
    @Override
    public int tabCompletion(String lastArgument, List<CharSequence> candidates) {
        int toReturn = FeedTabCompleter.tabCompletion(getArguments(), getContext(), lastArgument, candidates);
        return toReturn;
    }

}
