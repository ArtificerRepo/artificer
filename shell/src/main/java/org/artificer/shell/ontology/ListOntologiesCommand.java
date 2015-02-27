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

import org.artificer.client.ArtificerAtomApiClient;
import org.artificer.client.ontology.OntologySummary;
import org.artificer.shell.AbstractCommand;
import org.artificer.shell.i18n.Messages;
import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.command.invocation.CommandInvocation;

import java.util.List;

/**
 * Lists all ontologies in the S-RAMP repository.
 *
 * @author Brett Meyer
 * @author eric.wittmann@redhat.com
 */
@CommandDefinition(name = "list",
		description = "The \"list\" command displays a list of all the ontologies currently known to the Artificer repository.  This list may be empty if no ontologies have yet been added to the repository.\n")
public class ListOntologiesCommand extends AbstractCommand {

	@Override
	protected String getName() {
		return "list";
	}

	@Override
	protected CommandResult doExecute(CommandInvocation commandInvocation) throws Exception {
		ArtificerAtomApiClient client = client(commandInvocation);
		try {
			List<OntologySummary> ontologies = client.getOntologies();
			commandInvocation.getShell().out().println(Messages.i18n.format("ListOntologies.Summary", ontologies.size()));
			commandInvocation.getShell().out().println("  Idx, UUID, Base");
			commandInvocation.getShell().out().println("  ---------------");
			int idx = 1;
			for (OntologySummary ontology : ontologies) {
				commandInvocation.getShell().out().printf("  %d, %s, %s\n", idx++, ontology.getUuid(), ontology.getBase());
			}

			context(commandInvocation).setCurrentOntologyFeed(ontologies);
		} catch (Exception e) {
			commandInvocation.getShell().out().println(Messages.i18n.format("ListOntologies.Failed"));
			commandInvocation.getShell().out().println("\t" + e.getMessage());
            return CommandResult.FAILURE;
		}
        return CommandResult.SUCCESS;
	}
}
