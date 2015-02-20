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
package org.artificer.shell.ontology;

import org.apache.commons.lang.StringUtils;
import org.artificer.client.ArtificerAtomApiClient;
import org.artificer.client.ontology.OntologySummary;
import org.artificer.shell.i18n.Messages;
import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.cl.Option;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.command.invocation.CommandInvocation;

/**
 * Deletes an ontology.
 *
 * @author Brett Meyer
 * @author eric.wittmann@redhat.com
 */
@CommandDefinition(name = "delete",
		description = "The \"delete\" command removes a single ontology from the Artificer repository.\n")
public class DeleteOntologyCommand extends AbstractOntologyCommand {

	@Option(name = "uuid", hasValue = true, required = false,
			description = "Ontology UUID")
	private String ontologyUuid;

	@Option(name = "feed", hasValue = true, required = false,
			description = "Feed index")
	private String feedIndex;

	@Option(shortName = 'h', name = "help", hasValue = false, required = false, overrideRequired = true,
			description = "Display help")
	private boolean help;

	@Override
	protected String getName() {
		return "delete";
	}

	@Override
	protected CommandResult doExecute(CommandInvocation commandInvocation) throws Exception {
		if (help) {
			return doHelp(commandInvocation);
		}

		ArtificerAtomApiClient client = client(commandInvocation);

		if (StringUtils.isNotBlank(feedIndex)) {
			OntologySummary ontologySummary = ontologySummaryFromFeed(commandInvocation, feedIndex);
			ontologyUuid = ontologySummary.getUuid();
		} else if (StringUtils.isBlank(ontologyUuid)) {
			commandInvocation.getShell().out().println(Messages.i18n.format("Ontology.Arguments"));
			return CommandResult.FAILURE;
		}

		try {
			client.deleteOntology(ontologyUuid);
			commandInvocation.getShell().out().println(Messages.i18n.format("DeleteOntology.Deleted"));
		} catch (Exception e) {
			commandInvocation.getShell().out().println(Messages.i18n.format("DeleteOntology.DeleteFailed"));
			commandInvocation.getShell().out().println("\t" + e.getMessage());
            return CommandResult.FAILURE;
		}
		return CommandResult.SUCCESS;
	}
}
