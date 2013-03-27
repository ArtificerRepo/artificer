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

import org.overlord.sramp.client.SrampAtomApiClient;
import org.overlord.sramp.client.query.ArtifactSummary;
import org.overlord.sramp.client.query.QueryResultSet;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.shell.api.AbstractShellCommand;
import org.overlord.sramp.shell.api.InvalidCommandArgumentException;
import org.overlord.sramp.shell.util.FileNameCompleter;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;

/**
 * Deletes an artifact from the S-RAMP repository.
 *
 * @author eric.wittmann@redhat.com
 */
public class DeleteCommand extends AbstractShellCommand {

	/**
	 * Constructor.
	 */
	public DeleteCommand() {
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.ShellCommand#printUsage()
	 */
	@Override
	public void printUsage() {
        print("s-ramp:delete [<artifactId>]");
        print("\tValid formats for artifactId:");
        print("\t  feed:<feedIndex> - an index into the most recent feed");
        print("\t  uuid:<srampUUID> - the UUID of an s-ramp artifact");
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.ShellCommand#printHelp()
	 */
	@Override
	public void printHelp() {
        print("The 'delete' command removes an artifact from the S-RAMP");
        print("repository.  The artifact can be identified either by its");
        print("unique S-RAMP uuid or else by an index into the most recent");
        print("Feed.  Additionally, the currently active artifact can be");
        print("deleted by omitting the <artifactId> argument.");
        print("");
        print("Note: a Feed can be obtained, for example, by using the ");
        print("s-ramp:query command.");
        print("");
        print("Example usage:");
        print(">  s-ramp:delete");
        print(">  s-ramp:delete feed:1");
        print(">  s-ramp:delete uuid:2832-3183-2937-9983");
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.ShellCommand#execute()
	 */
	@Override
	public void execute() throws Exception {
		QName clientVarName = new QName("s-ramp", "client");
		QName artifactVarName = new QName("s-ramp", "artifact");
        QName feedVarName = new QName("s-ramp", "feed");

		SrampAtomApiClient client = (SrampAtomApiClient) getContext().getVariable(clientVarName);
		if (client == null) {
			print("No S-RAMP repository connection is currently open.");
			return;
		}

        BaseArtifactType artifact = null;
		String artifactIdArg = this.optionalArgument(0);
        if (artifactIdArg == null) {
            artifact = (BaseArtifactType) getContext().getVariable(artifactVarName);
            if (artifact == null) {
                print("No active S-RAMP artifact exists.  Use s-ramp:getMetaData.");
                return;
            }
        } else {
    		String idType = artifactIdArg.substring(0, artifactIdArg.indexOf(':'));
    		if ("feed".equals(idType)) {
    		    QueryResultSet rset = (QueryResultSet) getContext().getVariable(feedVarName);
    		    int feedIdx = Integer.parseInt(artifactIdArg.substring(artifactIdArg.indexOf(':')+1)) - 1;
    		    if (feedIdx < 0 || feedIdx >= rset.size()) {
    		        throw new InvalidCommandArgumentException(0, "Feed index out of range.");
    		    }
    		    ArtifactSummary summary = rset.get(feedIdx);
    		    String artifactUUID = summary.getUuid();
    		    artifact = client.getArtifactMetaData(summary.getType(), artifactUUID);
    		} else if ("uuid".equals(idType)) {
    		    throw new InvalidCommandArgumentException(0, "uuid: style artifact identifiers not yet implemented.");
    		} else {
    		    throw new InvalidCommandArgumentException(0, "Invalid artifact id format.");
    		}
		}

		try {
			client.deleteArtifact(artifact.getUuid(), ArtifactType.valueOf(artifact));
			print("Successfully deleted artifact '%1$s'.", artifact.getName());
		} catch (Exception e) {
			print("FAILED to delete the artifact.");
			print("\t" + e.getMessage());
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
        } else if (getArguments().size() == 1) {
            if (lastArgument == null)
                lastArgument = "";
            FileNameCompleter delegate = new FileNameCompleter();
            return delegate.complete(lastArgument, lastArgument.length(), candidates);
        } else {
            return -1;
        }
    }

}
