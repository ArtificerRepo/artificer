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
package org.artificer.shell.core;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.artificer.client.ArtificerAtomApiClient;
import org.artificer.common.query.ArtifactSummary;
import org.artificer.client.query.QueryResultSet;
import org.artificer.common.ArtifactType;
import org.artificer.common.ArtifactTypeEnum;
import org.artificer.shell.AbstractCommand;
import org.artificer.shell.i18n.Messages;
import org.jboss.aesh.cl.Arguments;
import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.cl.Option;
import org.jboss.aesh.cl.completer.OptionCompleter;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.command.completer.CompleterInvocation;
import org.jboss.aesh.console.command.invocation.CommandInvocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Performs a query against the s-ramp server and displays the result.
 *
 * @author Brett Meyer
 * @author eric.wittmann@redhat.com
 */
@CommandDefinition(name = "query",
		description = "The \"query\" command issues a standard Artificer formatted query against the Artificer server.  The query will result in a Feed of entries.\n")
public class QueryCommand extends AbstractCommand {

	@Arguments(description = "<query>", completer = Completer.class)
	private List<String> arguments;

	@Option(shortName = 'h', name = "help", hasValue = false, required = false, overrideRequired = true,
			description = "Display help")
	private boolean help;

	@Override
	protected CommandResult doExecute(CommandInvocation commandInvocation) throws Exception {
		if (help) {
			return doHelp(commandInvocation);
		}
		if (CollectionUtils.isEmpty(arguments)) {
			return doHelp(commandInvocation);
		}

		String query = requiredArgument(commandInvocation, arguments, 0);

		ArtificerAtomApiClient client = client(commandInvocation);
		if (query.endsWith("/")) {
			query = query.substring(0, query.length() - 1);
		}

		commandInvocation.getShell().out().println(Messages.i18n.format("Query.Querying"));
		commandInvocation.getShell().out().println("\t" + query);
		try {
    		QueryResultSet rset = client.query(query, 0, 100, "uuid", true);
    		int entryIndex = 1;
			commandInvocation.getShell().out().println(Messages.i18n.format("Query.AtomFeedSummary", rset.size()));
			commandInvocation.getShell().out().println("  Idx, UUID, Type, Name");
			commandInvocation.getShell().out().println("  ---------------------");
    		for (ArtifactSummary summary : rset) {
    			ArtifactType type = summary.getArtifactType();
    			String displayType = type.getArtifactType().getType().toString();
    			if (type.isExtendedType() && type.getExtendedType() != null) {
    			    displayType = type.getExtendedType();
    			}
				commandInvocation.getShell().out().printf("  %d, %s, %s, %s\n", entryIndex++, summary.getUuid(),
						displayType, summary.getName());
    		}
    		context(commandInvocation).setCurrentArtifactFeed(rset);
		} catch (Exception e) {
			commandInvocation.getShell().out().println(Messages.i18n.format("Query.Failure"));
			commandInvocation.getShell().out().println("\t" + e.getMessage());
			return CommandResult.FAILURE;
		}
        return CommandResult.SUCCESS;
	}

	private static class Completer implements OptionCompleter<CompleterInvocation> {
		@Override
		public void complete(CompleterInvocation completerInvocation) {
			QueryCommand command = (QueryCommand) completerInvocation.getCommand();
			if (CollectionUtils.isEmpty(command.arguments)) {
				String currentValue = completerInvocation.getGivenCompleteValue();
				if (StringUtils.isBlank(currentValue)) {
					completerInvocation.setAppendSpace(false);
					completerInvocation.addCompleterValue("/s-ramp/");
				} else {
					String [] split = currentValue.split("/");
					if (split.length == 0 || split.length == 1 || (split.length == 2 && !currentValue.endsWith("/"))) {
						completerInvocation.setAppendSpace(false);
						completerInvocation.addCompleterValue("/s-ramp/");
					}
					// All artifact models
					if (currentValue.equals("/s-ramp/")) {
						Set<String> modelCandidates = new TreeSet<>();
						for (ArtifactTypeEnum t : ArtifactTypeEnum.values()) {
							modelCandidates.add(t.getModel());
						}
						completerInvocation.addAllCompleterValues(new ArrayList<>(modelCandidates));
					}
					// Artifact models matching the partial value
					if (split.length == 3 && !currentValue.endsWith("/") && currentValue.startsWith("/s-ramp/")) { //$NON-NLS-2$
						String partialModel = split[2];
						Set<String> modelCandidates = new TreeSet<>();
						for (ArtifactTypeEnum t : ArtifactTypeEnum.values()) {
							if (t.getModel().startsWith(partialModel))
								modelCandidates.add(t.getModel());
						}
						if (modelCandidates.size() == 1) {
							completerInvocation.setAppendSpace(false);
							completerInvocation.addCompleterValue("/s-ramp/" + modelCandidates.iterator().next() + "/");
						} else {
							completerInvocation.addAllCompleterValues(new ArrayList<>(modelCandidates));
						}
					}
					// All artifact types
					if (split.length == 3 && currentValue.endsWith("/") && currentValue.startsWith("/s-ramp/")) { //$NON-NLS-2$
						String model = split[2];
						Set<String> typeCandidates = new TreeSet<>();
						for (ArtifactTypeEnum t : ArtifactTypeEnum.values()) {
							if (t.getModel().equals(model)) {
								typeCandidates.add(t.getType());
							}
						}
						completerInvocation.addAllCompleterValues(new ArrayList<>(typeCandidates));
					}
					// Artifact types matching the partial value
					if (split.length == 4 && !currentValue.endsWith("/") && currentValue.startsWith("/s-ramp/")) { //$NON-NLS-2$
						String model = split[2];
						String partialType = split[3];
						Set<String> typeCandidates = new TreeSet<>();
						for (ArtifactTypeEnum t : ArtifactTypeEnum.values()) {
							if (t.getModel().equals(model) && t.getType().startsWith(partialType)) {
								typeCandidates.add(t.getType());
							}
						}
						if (typeCandidates.size() == 1) {
							completerInvocation.setAppendSpace(false);
							completerInvocation.addCompleterValue("/s-ramp/" + model + "/" + typeCandidates.iterator().next() + "/");
						} else {
							completerInvocation.addAllCompleterValues(new ArrayList<>(typeCandidates));
						}
					}
				}
			}
		}
	}

	@Override
	protected String getName() {
		return "query";
	}

}
