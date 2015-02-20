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

import org.artificer.client.ArtificerAtomApiClient;
import org.artificer.common.ArtifactType;
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
 * Refreshes the full meta-data for a single artifact - namely the currently active
 * artifact in the session.
 *
 * @author Brett Meyer
 */
@CommandDefinition(name = "refreshMetaData",
		description = "The \"refreshMetaData\" command downloads the latest meta-data for a single artifact from the Artificer repository.  The artifact in question is the currently active artifact in the session.  If no artifact is currently active, then this command will fail.  This essentially re-downloads the meta-data for the current artifact and replaces any changes that may have existed there.\n")
public class RefreshMetaDataCommand extends AbstractCommand {

	@Option(shortName = 'h', name = "help", hasValue = false, required = false, overrideRequired = true,
			description = "Display help")
	private boolean help;

	@Override
	protected CommandResult doExecute(CommandInvocation commandInvocation) throws Exception {
		if (help) {
			return doHelp(commandInvocation);
		}

		ArtificerAtomApiClient client = client(commandInvocation);
		BaseArtifactType artifact = currentArtifact(commandInvocation);

		try {
			ArtifactType type = ArtifactType.valueOf(artifact);
			BaseArtifactType metaData = client.getArtifactMetaData(type, artifact.getUuid());
			context(commandInvocation).setCurrentArtifact(metaData);
			commandInvocation.getShell().out().println(Messages.i18n.format("RefreshMetaData.Success", metaData.getName())); //$NON-NLS-1$
			commandInvocation.getShell().out().println(Messages.i18n.format("RefreshMetaData.MetaDataFor", metaData.getUuid())); //$NON-NLS-1$
			commandInvocation.getShell().out().println("--------------"); //$NON-NLS-1$
			PrintArtifactMetaDataVisitor visitor = new PrintArtifactMetaDataVisitor(commandInvocation);
			ArtifactVisitorHelper.visitArtifact(visitor, metaData);
		} catch (Exception e) {
			commandInvocation.getShell().out().println(Messages.i18n.format("RefreshMetaData.Failure")); //$NON-NLS-1$
			commandInvocation.getShell().out().println("\t" + e.getMessage()); //$NON-NLS-1$
            return CommandResult.FAILURE;
		}
        return CommandResult.SUCCESS;
	}

	@Override
	protected String getName() {
		return "refreshMetaData";
	}

}
