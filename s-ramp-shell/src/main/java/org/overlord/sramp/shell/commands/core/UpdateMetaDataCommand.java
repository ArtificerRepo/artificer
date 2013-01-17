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

import javax.xml.namespace.QName;

import org.overlord.sramp.client.SrampAtomApiClient;
import org.overlord.sramp.shell.AbstractShellCommand;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;

/**
 * Updates an artifact's meta-data in the s-ramp repository. This requires an active artifact to exist in the
 * context, which was presumably modified in some way (updated core meta-data, properties, relationships,
 * etc).
 *
 * @author eric.wittmann@redhat.com
 */
public class UpdateMetaDataCommand extends AbstractShellCommand {

	/**
	 * Constructor.
	 */
	public UpdateMetaDataCommand() {
	}

	/**
	 * @see org.overlord.sramp.common.shell.ShellCommand#printUsage()
	 */
	@Override
	public void printUsage() {
		print("s-ramp:updateMetaData");
	}

	/**
	 * @see org.overlord.sramp.common.shell.ShellCommand#printHelp()
	 */
	@Override
	public void printHelp() {
		print("The 'updateMetaData' command updates the meta-data of the currently active");
		print("artifact in the context.  Whatever changes were made to the active");
		print("artifact will be sent back to the S-RAMP repository.");
		print("");
		print("Example usage:");
		print(">  s-ramp:updateMetaData");
	}

	/**
	 * @see org.overlord.sramp.common.shell.ShellCommand#execute()
	 */
	@Override
	public void execute() throws Exception {
		QName clientVarName = new QName("s-ramp", "client");
		QName artifactVarName = new QName("s-ramp", "artifact");

		SrampAtomApiClient client = (SrampAtomApiClient) getContext().getVariable(clientVarName);
		if (client == null) {
			print("No S-RAMP repository connection is currently open.");
			return;
		}

		BaseArtifactType artifact = (BaseArtifactType) getContext().getVariable(artifactVarName);
		if (artifact == null) {
			print("No active S-RAMP artifact exists.  Use s-ramp:getMetaData.");
			return;
		}

		try {
			client.updateArtifactMetaData(artifact);
			print("Successfully updated artifact %1$s.", artifact.getName());
		} catch (Exception e) {
			print("FAILED to update the artifact.");
			print("\t" + e.getMessage());
		}
	}

}
