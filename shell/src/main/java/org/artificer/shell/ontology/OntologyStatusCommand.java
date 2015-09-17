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

import org.apache.commons.collections.CollectionUtils;
import org.artificer.client.ArtificerAtomApiClient;
import org.artificer.client.ontology.OntologySummary;
import org.artificer.shell.common.AbstractCommand;
import org.artificer.shell.i18n.Messages;
import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.command.invocation.CommandInvocation;

import java.util.List;

/**
 * @author Brett Meyer
 */
@CommandDefinition(name = "status",
		description = "The \"status\" command displays the current Artificer Ontology status.\n")
public class OntologyStatusCommand extends AbstractCommand {

    @Override
    protected String getName() {
        return "ontology status";
    }

	@Override
	protected CommandResult doExecute(CommandInvocation commandInvocation) throws Exception {
		ArtificerAtomApiClient client = client(commandInvocation);
		List<OntologySummary> ontologies = context(commandInvocation).getCurrentOntologyFeed();

		if (client == null) {
			commandInvocation.getShell().out().println(Messages.i18n.format("Status.Status1"));
		} else {
			commandInvocation.getShell().out().println(Messages.i18n.format("Status.Status2", client.getEndpoint()));
		}

		if (CollectionUtils.isEmpty(ontologies)) {
			commandInvocation.getShell().out().println(Messages.i18n.format("OntologyStatus.Status1"));
		} else {
			commandInvocation.getShell().out().println(Messages.i18n.format("OntologyStatus.Status2", ontologies.size()));
		}

        return CommandResult.SUCCESS;
	}

}
