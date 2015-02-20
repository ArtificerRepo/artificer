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

import org.artificer.common.visitors.ArtifactVisitorHelper;
import org.artificer.shell.AbstractCommand;
import org.artificer.shell.i18n.Messages;
import org.artificer.shell.util.PrintArtifactMetaDataVisitor;
import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.cl.Option;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.command.invocation.CommandInvocation;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;

/**
 * Shows the full meta-data for the artifact currently active in the
 * session/context.
 *
 * @author Brett Meyer
 */
@CommandDefinition(name = "showMetaData",
		description = "The \"showMetaData\" command prints out the meta-data for the artifact currently active in the session.\n")
public class ShowMetaDataCommand extends AbstractCommand {

	@Option(shortName = 'h', name = "help", hasValue = false, required = false, overrideRequired = true,
			description = "Display help")
	private boolean help;

	@Override
	protected CommandResult doExecute(CommandInvocation commandInvocation) throws Exception {
		if (help) {
			return doHelp(commandInvocation);
		}

		BaseArtifactType artifact = currentArtifact(commandInvocation);

		// Print out the meta-data information
		commandInvocation.getShell().out().println(Messages.i18n.format("RefreshMetaData.MetaDataFor", artifact.getUuid())); //$NON-NLS-1$
		commandInvocation.getShell().out().println("--------------"); //$NON-NLS-1$
		PrintArtifactMetaDataVisitor visitor = new PrintArtifactMetaDataVisitor(commandInvocation);
		ArtifactVisitorHelper.visitArtifact(visitor, artifact);
        return CommandResult.SUCCESS;
	}

	@Override
	protected String getName() {
		return "showMetaData";
	}

}
