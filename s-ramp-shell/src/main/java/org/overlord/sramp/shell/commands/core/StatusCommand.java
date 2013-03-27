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
import org.overlord.sramp.client.query.QueryResultSet;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.shell.api.AbstractShellCommand;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;

/**
 * Displays a summary of the current status, including what repository the
 * user is currently connected to (if any).
 *
 * @author eric.wittmann@redhat.com
 */
public class StatusCommand extends AbstractShellCommand {

	/**
	 * Constructor.
	 */
	public StatusCommand() {
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.ShellCommand#printUsage()
	 */
	@Override
	public void printUsage() {
		print("s-ramp:status");
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.ShellCommand#printHelp()
	 */
	@Override
	public void printHelp() {
		print("The 'status' command displays the current S-RAMP status.");
		print("");
		print("Example usage:");
		print(">  s-ramp:status");
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.ShellCommand#execute()
	 */
	@Override
	public void execute() throws Exception {
		QName clientVarName = new QName("s-ramp", "client");
		QName artifactVarName = new QName("s-ramp", "artifact");
		QName feedVarName = new QName("s-ramp", "feed");

		SrampAtomApiClient client = (SrampAtomApiClient) getContext().getVariable(clientVarName);
		BaseArtifactType artifact = (BaseArtifactType) getContext().getVariable(artifactVarName);
		QueryResultSet feed = (QueryResultSet) getContext().getVariable(feedVarName);

		if (client == null) {
			print (" S-RAMP Connection: <not currently connected>");
		} else {
			print (" S-RAMP Connection: %1$s", client.getEndpoint());
		}

		if (artifact == null) {
			print("   S-RAMP Artifact: <none currently active>");
		} else {
			ArtifactType type = ArtifactType.valueOf(artifact);
			print("   S-RAMP Artifact: %1$s (%2$s)", artifact.getName(), type.getType());
		}

		if (feed == null) {
			print("     Artifact Feed: <none currently active>");
		} else {
			print("     Artifact Feed: %1$d items", feed.size());
		}
	}

}
