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
import org.overlord.sramp.shell.api.AbstractShellCommand;
import org.overlord.sramp.shell.api.InvalidCommandArgumentException;

/**
 * Displays the audit trail for an artifact.
 *
 * @author eric.wittmann@redhat.com
 */
public class ShowAuditTrailCommand extends AbstractShellCommand {

	/**
	 * Constructor.
	 */
	public ShowAuditTrailCommand() {
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.ShellCommand#printUsage()
	 */
	@Override
	public void printUsage() {
        print("audit:showAuditTrail <artifactId>");
        print("\tValid formats for artifactId:");
        print("\t  feed:<feedIndex> - an index into the most recent feed");
        print("\t  uuid:<srampUUID> - the UUID of an s-ramp artifact");
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.ShellCommand#printHelp()
	 */
	@Override
	public void printHelp() {
		print("The 'showAuditTrail' command displays a list of all the audit");
		print("entries for a given artifact.  Use this command to see how an");
        print("artifact has been modified over time.");
		print("");
		print("Example usage:");
        print("> audit:showAuditTrail feed:3");
        print("> audit:showAuditTrail uuid:7387-28732-9183-92737");
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.ShellCommand#execute()
	 */
	@Override
	public void execute() throws Exception {
        String artifactIdArg = this.requiredArgument(0, "Please specify a valid artifact identifier.");
        if (!artifactIdArg.contains(":")) {
            throw new InvalidCommandArgumentException(0, "Invalid artifact id format.");
        }
        QName clientVarName = new QName("s-ramp", "client");
        QName feedVarName = new QName("s-ramp", "feed");
        SrampAtomApiClient client = (SrampAtomApiClient) getContext().getVariable(clientVarName);
        if (client == null) {
            print("No S-RAMP repository connection is currently open.");
            return;
        }

        String artifactUuid = null;
        String idType = artifactIdArg.substring(0, artifactIdArg.indexOf(':'));
        if ("feed".equals(idType)) {
            QueryResultSet rset = (QueryResultSet) getContext().getVariable(feedVarName);
            int feedIdx = Integer.parseInt(artifactIdArg.substring(artifactIdArg.indexOf(':')+1)) - 1;
            if (feedIdx < 0 || feedIdx >= rset.size()) {
                throw new InvalidCommandArgumentException(0, "Feed index out of range.");
            }
            ArtifactSummary summary = rset.get(feedIdx);
            artifactUuid = summary.getUuid();
        } else if ("uuid".equals(idType)) {
//          String artifactUUID = artifactIdArg.substring(artifactIdArg.indexOf(':') + 1);
//          artifact = getArtifactMetaDataByUUID(client, artifactUUID);
            throw new InvalidCommandArgumentException(0, "uuid: style artifact identifiers not yet implemented.");
        } else {
            throw new InvalidCommandArgumentException(0, "Invalid artifact id format.");
        }

        AuditResultSet auditTrail = client.getAuditTrailForArtifact(artifactUuid);
        QName artifactVarName = new QName("audit", "auditTrail");
        getContext().setVariable(artifactVarName, auditTrail);
        print("Artifact Audit Trail (%1$d entries)", auditTrail.size());
        print("  Idx  Audit Entry");
        print("  ---  -----------");
        int idx = 1;
        for (AuditEntrySummary auditEntrySummary : auditTrail) {
            print("  %1$3d  %2$s", idx++, auditEntrySummary.toString());
        }
	}

    /**
     * @see org.overlord.sramp.shell.api.shell.AbstractShellCommand#tabCompletion(java.lang.String, java.util.List)
     */
    @Override
    public int tabCompletion(String lastArgument, List<CharSequence> candidates) {
        if (getArguments().isEmpty() && (lastArgument == null || "feed:".startsWith(lastArgument))) {
            QName feedVarName = new QName("s-ramp", "feed");
            QueryResultSet rset = (QueryResultSet) getContext().getVariable(feedVarName);
            if (rset != null) {
                for (int idx = 0; idx < rset.size(); idx++) {
                    String candidate = "feed:" + (idx+1);
                    if (lastArgument == null) {
                        candidates.add(candidate);
                    }
                    if (lastArgument != null && candidate.startsWith(lastArgument)) {
                        candidates.add(candidate);
                    }
                }
            }
            return 0;
        } else {
            return -1;
        }
    }

}
