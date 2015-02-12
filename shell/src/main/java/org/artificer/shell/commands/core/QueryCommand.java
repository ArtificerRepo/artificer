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
package org.artificer.shell.commands.core;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.namespace.QName;

import org.artificer.client.ArtificerAtomApiClient;
import org.artificer.client.query.ArtifactSummary;
import org.artificer.client.query.QueryResultSet;
import org.artificer.common.ArtifactType;
import org.artificer.common.ArtifactTypeEnum;
import org.artificer.shell.BuiltInShellCommand;
import org.artificer.shell.i18n.Messages;
import org.artificer.shell.CompletionConstants;
import org.artificer.shell.api.InvalidCommandArgumentException;

/**
 * Performs a query against the s-ramp server and displays the result.
 *
 * @author eric.wittmann@redhat.com
 */
public class QueryCommand extends BuiltInShellCommand {

	/**
	 * Constructor.
	 */
	public QueryCommand() {
	}

	@Override
	public boolean execute() throws Exception {
		String queryArg = this.requiredArgument(0, Messages.i18n.format("Query.InvalidArgMsg.MissingQuery")); //$NON-NLS-1$
		String tooManyArgs = this.optionalArgument(1);
		if (tooManyArgs != null) {
            throw new InvalidCommandArgumentException(1, Messages.i18n.format("Query.TooManyArgs")); //$NON-NLS-1$
		}

		// Get the client out of the context and exec the query
		QName varName = new QName("s-ramp", "client"); //$NON-NLS-1$ //$NON-NLS-2$
		ArtificerAtomApiClient client = (ArtificerAtomApiClient) getContext().getVariable(varName);
		if (client == null) {
			print(Messages.i18n.format("MissingArtificerConnection")); //$NON-NLS-1$
			return false;
		}
		if (queryArg.endsWith("/")) { //$NON-NLS-1$
			queryArg = queryArg.substring(0, queryArg.length() - 1);
		}

		print(Messages.i18n.format("Query.Querying")); //$NON-NLS-1$
		print("\t" + queryArg); //$NON-NLS-1$
		try {
    		QueryResultSet rset = client.query(queryArg, 0, 100, "uuid", true); //$NON-NLS-1$
    		int entryIndex = 1;
    		print(Messages.i18n.format("Query.AtomFeedSummary", rset.size())); //$NON-NLS-1$
    		print("  Idx                    Type Name"); //$NON-NLS-1$
    		print("  ---                    ---- ----"); //$NON-NLS-1$
    		for (ArtifactSummary summary : rset) {
    			ArtifactType type = summary.getType();
    			String displayType = type.getArtifactType().getType().toString();
    			if (type.isExtendedType() && type.getExtendedType() != null) {
    			    displayType = type.getExtendedType();
    			}
                print("  %1$3d %2$23s %3$-40s", entryIndex++, displayType, //$NON-NLS-1$
    					summary.getName());
    		}
    		getContext().setVariable(new QName("s-ramp", "feed"), rset); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (Exception e) {
            print(Messages.i18n.format("Query.Failure")); //$NON-NLS-1$
            print("\t" + e.getMessage()); //$NON-NLS-1$
            return false;
		}
        return true;
	}

	@Override
	public int tabCompletion(String lastArgument, List<CharSequence> candidates) {
		if (getArguments().isEmpty()) {
			if (lastArgument == null) {
				candidates.add("\"/s-ramp/"); //$NON-NLS-1$
			} else {
				String [] split = lastArgument.split("/"); //$NON-NLS-1$
				if (split.length == 0 || split.length == 1 || (split.length == 2 && !lastArgument.endsWith("/"))) { //$NON-NLS-1$
					candidates.add("\"/s-ramp/"); //$NON-NLS-1$
				}
				// All artifact models
				if (lastArgument.equals("/s-ramp/")) { //$NON-NLS-1$
					Set<String> modelCandidates = new TreeSet<String>();
					for (ArtifactTypeEnum t : ArtifactTypeEnum.values()) {
						modelCandidates.add(t.getModel());
					}
					candidates.addAll(modelCandidates);
				}
				// Artifact models matching the partial value
				if (split.length == 3 && !lastArgument.endsWith("/") && lastArgument.startsWith("/s-ramp/")) { //$NON-NLS-1$ //$NON-NLS-2$
					String partialModel = split[2];
					Set<String> modelCandidates = new TreeSet<String>();
					for (ArtifactTypeEnum t : ArtifactTypeEnum.values()) {
						if (t.getModel().startsWith(partialModel))
							modelCandidates.add(t.getModel());
					}
					if (modelCandidates.size() == 1) {
						candidates.add(modelCandidates.iterator().next() + "/"); //$NON-NLS-1$
					} else {
						candidates.addAll(modelCandidates);
					}
				}
				// All artifact types
				if (split.length == 3 && lastArgument.endsWith("/") && lastArgument.startsWith("/s-ramp/")) { //$NON-NLS-1$ //$NON-NLS-2$
					String model = split[2];
					Set<String> typeCandidates = new TreeSet<String>();
					for (ArtifactTypeEnum t : ArtifactTypeEnum.values()) {
						if (t.getModel().equals(model)) {
							typeCandidates.add(t.getType());
						}
					}
					candidates.addAll(typeCandidates);
				}
				// Artifact types matching the partial value
				if (split.length == 4 && !lastArgument.endsWith("/") && lastArgument.startsWith("/s-ramp/")) { //$NON-NLS-1$ //$NON-NLS-2$
					String model = split[2];
					String partialType = split[3];
					Set<String> typeCandidates = new TreeSet<String>();
					for (ArtifactTypeEnum t : ArtifactTypeEnum.values()) {
						if (t.getModel().equals(model) && t.getType().startsWith(partialType)) {
							typeCandidates.add(t.getType());
						}
					}
					candidates.addAll(typeCandidates);
				}
			}
		}
        return CompletionConstants.NO_APPEND_SEPARATOR;
	}

}
