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
import java.util.Set;
import java.util.TreeSet;

import javax.xml.namespace.QName;

import org.overlord.sramp.client.SrampAtomApiClient;
import org.overlord.sramp.client.query.ArtifactSummary;
import org.overlord.sramp.client.query.QueryResultSet;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.common.ArtifactTypeEnum;
import org.overlord.sramp.shell.AbstractShellCommand;
import org.overlord.sramp.shell.commands.Arguments;

/**
 * Performs a query against the s-ramp server and displays the result.
 *
 * @author eric.wittmann@redhat.com
 */
public class QueryCommand extends AbstractShellCommand {

	/**
	 * Constructor.
	 */
	public QueryCommand() {
	}

	/**
	 * @see org.overlord.sramp.common.shell.ShellCommand#printUsage()
	 */
	@Override
	public void printUsage() {
		print("s-ramp:query <srampQuery>");
	}

	/**
	 * @see org.overlord.sramp.common.shell.ShellCommand#printHelp()
	 */
	@Override
	public void printHelp() {
		print("The 'query' command issues a standard S-RAMP formatted");
		print("query against the S-RAMP server.  The query will result");
		print("in a Feed of entries.");
		print("");
		print("Example usage:");
		print(">  s-ramp:query /s-ramp/wsdl/WsdlDocument");
		print(">  s-ramp:query \"/s-ramp/wsdl[@name = 'find']\"");
	}

	/**
	 * @see org.overlord.sramp.common.shell.ShellCommand#execute()
	 */
	@Override
	public void execute() throws Exception {
		String queryArg = this.requiredArgument(0, "Please specify a valid S-RAMP query.");

		// Now process any additiona args - if there are any, then the user didn't
		// surround the query with quotes - so mash them all together.
		Arguments args = getArguments();
		args.remove(0);
		if (!args.isEmpty()) {
    		for (String queryFragment : args) {
                queryArg += " " + queryFragment;
            }
		}

		// Get the client out of the context and exec the query
		QName varName = new QName("s-ramp", "client");
		SrampAtomApiClient client = (SrampAtomApiClient) getContext().getVariable(varName);
		if (client == null) {
			print("No S-RAMP repository connection is currently open.");
			return;
		}
		if (queryArg.endsWith("/")) {
			queryArg = queryArg.substring(0, queryArg.length() - 1);
		}

		print("Querying the S-RAMP repository:");
		print("\t" + queryArg);
		try {
    		QueryResultSet rset = client.query(queryArg, 0, 100, "uuid", true);
    		int entryIndex = 1;
    		print("Atom Feed (%1$d entries)", rset.size());
    		print("  Idx                    Type Name");
    		print("  ---                    ---- ----");
    		for (ArtifactSummary summary : rset) {
    			ArtifactType type = summary.getType();
    			String displayType = type.getArtifactType().getType().toString();
    			if (type.isUserDefinedType() && type.getUserType() != null) {
    			    displayType = type.getUserType();
    			}
                print("  %1$3d %2$23s %3$-40s", entryIndex++, displayType,
    					summary.getName());
    		}
    		getContext().setVariable(new QName("s-ramp", "feed"), rset);
		} catch (Exception e) {
            print("FAILED to query the repository.");
            print("\t" + e.getMessage());
		}
	}

	/**
	 * @see org.overlord.sramp.common.shell.AbstractShellCommand#tabCompletion(java.lang.String, java.util.List)
	 */
	@Override
	public int tabCompletion(String lastArgument, List<CharSequence> candidates) {
		if (getArguments().isEmpty()) {
			if (lastArgument == null) {
				candidates.add("/s-ramp/");
				return 0;
			} else {
				String [] split = lastArgument.split("/");
				if (split.length == 0 || split.length == 1 || (split.length == 2 && !lastArgument.endsWith("/"))) {
					candidates.add("/s-ramp/");
					return 0;
				}
				// All artifact models
				if (lastArgument.equals("/s-ramp/")) {
					Set<String> modelCandidates = new TreeSet<String>();
					for (ArtifactTypeEnum t : ArtifactTypeEnum.values()) {
						modelCandidates.add(t.getModel());
					}
					candidates.addAll(modelCandidates);
					return lastArgument.length();
				}
				// Artifact models matching the partial value
				if (split.length == 3 && !lastArgument.endsWith("/") && lastArgument.startsWith("/s-ramp/")) {
					String partialModel = split[2];
					Set<String> modelCandidates = new TreeSet<String>();
					for (ArtifactTypeEnum t : ArtifactTypeEnum.values()) {
						if (t.getModel().startsWith(partialModel))
							modelCandidates.add(t.getModel());
					}
					if (modelCandidates.size() == 1) {
						candidates.add(modelCandidates.iterator().next() + "/");
					} else {
						candidates.addAll(modelCandidates);
					}

					return lastArgument.length() - partialModel.length();
				}
				// All artifact types
				if (split.length == 3 && lastArgument.endsWith("/") && lastArgument.startsWith("/s-ramp/")) {
					String model = split[2];
					Set<String> typeCandidates = new TreeSet<String>();
					for (ArtifactTypeEnum t : ArtifactTypeEnum.values()) {
						if (t.getModel().equals(model)) {
							typeCandidates.add(t.getType());
						}
					}
					candidates.addAll(typeCandidates);
					return lastArgument.length();
				}
				// Artifact types matching the partial value
				if (split.length == 4 && !lastArgument.endsWith("/") && lastArgument.startsWith("/s-ramp/")) {
					String model = split[2];
					String partialType = split[3];
					Set<String> typeCandidates = new TreeSet<String>();
					for (ArtifactTypeEnum t : ArtifactTypeEnum.values()) {
						if (t.getModel().equals(model) && t.getType().startsWith(partialType)) {
							typeCandidates.add(t.getType());
						}
					}
					candidates.addAll(typeCandidates);
					return lastArgument.length() - partialType.length();
				}
			}
		}
		return -1;
	}

}
