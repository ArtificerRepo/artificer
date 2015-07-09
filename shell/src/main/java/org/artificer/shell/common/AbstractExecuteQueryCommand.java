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
package org.artificer.shell.common;

import org.apache.commons.collections.CollectionUtils;
import org.artificer.client.ArtificerAtomApiClient;
import org.artificer.client.query.QueryResultSet;
import org.artificer.common.ArtifactType;
import org.artificer.common.query.ArtifactSummary;
import org.artificer.shell.i18n.Messages;
import org.jboss.aesh.cl.Option;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.command.invocation.CommandInvocation;

import java.util.List;

/**
 * Provides common logic needed for commands that execute a query (ad-hoc, stored, etc.).  Assumes the command
 * requires a single argument to identify the query.
 *
 * @author Brett Meyer.
 */
public abstract class AbstractExecuteQueryCommand extends AbstractCommand {

	@Option(name = "startIndex", hasValue = true, required = false,
			description = "Paging: start index (begins at 0)")
	private Integer startIndex;

	@Option(name = "count", hasValue = true, required = false,
			description = "Paging: count (# to include on page)")
	private Integer count;

	@Option(name = "page", hasValue = true, required = false,
			description = "Paging: page # (assumes 0 startIndex and 100 count, unless startIndex/count provided now or in the past)")
	private Integer page;

	@Option(name = "orderBy", hasValue = true, required = false,
			description = "Sort by this field (defaults to uuid)", defaultValue = "uuid")
	private String orderBy;

	@Option(name = "ascending", hasValue = false, required = false,
			description = "Sort ascending (default)")
	private Boolean ascending;

	@Option(name = "descending", hasValue = false, required = false,
			description = "Sort descending")
	private Boolean descending;

	private CommandInvocation commandInvocation;

	@Override
	protected CommandResult doExecute(CommandInvocation commandInvocation) throws Exception {
		if (CollectionUtils.isEmpty(getArguments())) {
			return doHelp(commandInvocation);
		}

		this.commandInvocation = commandInvocation;

		if (startIndex != null) {
			context(commandInvocation).setCurrentStartIndex(startIndex);
		}
		if (count != null) {
			context(commandInvocation).setCurrentCount(count);
		}

		String argument = this.requiredArgument(commandInvocation, getArguments(), 0);

		ArtificerAtomApiClient client = client(commandInvocation);

		try {
			QueryResultSet rset = doExecute(argument, client, commandInvocation);

			commandInvocation.getShell().out().println();
			int entryIndex = 1;
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
			commandInvocation.getShell().out().println();
			long endIndex = rset.getStartIndex() + rset.size() - 1;
			commandInvocation.getShell().out().println(Messages.i18n.format("Query.AtomFeedSummary",
					rset.getStartIndex(), endIndex, rset.getTotalResults()));
			commandInvocation.getShell().out().println();

			context(commandInvocation).setCurrentArtifactFeed(rset);

			return CommandResult.SUCCESS;
		} catch (Exception e) {
			commandInvocation.getShell().out().println(Messages.i18n.format("Query.Failure"));
			commandInvocation.getShell().out().println("\t" + e.getMessage());
			return CommandResult.FAILURE;
		}
	}

	protected int getStartIndex() {
		int startIndex = context(commandInvocation).getCurrentStartIndex();

		if (page != null) {
			return (page.intValue() - 1) * getCount();
		} else {
			return startIndex;
		}
	}

	protected int getCount() {
		return context(commandInvocation).getCurrentCount();
	}

	protected String getOrderBy() {
		return orderBy;
	}

	protected boolean isAscending() {
		if (descending != null && Boolean.TRUE.equals(descending)) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Execute the query, using command-specific logic, using the given argument and client.
	 *
	 * @param argument
	 * @param client
	 * @param commandInvocation
	 * @return QueryResultSet
	 * @throws Exception
	 */
	protected abstract QueryResultSet doExecute(String argument, ArtificerAtomApiClient client,
			CommandInvocation commandInvocation) throws Exception;

	protected abstract List<String> getArguments();
}
