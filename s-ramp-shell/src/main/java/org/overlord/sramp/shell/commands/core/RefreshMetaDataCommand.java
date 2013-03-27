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
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.common.visitors.ArtifactVisitorHelper;
import org.overlord.sramp.shell.api.AbstractShellCommand;
import org.overlord.sramp.shell.util.PrintArtifactMetaDataVisitor;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;

/**
 * Refreshes the full meta-data for a single artifact - namely the currently active
 * artifact in the session.
 *
 * @author eric.wittmann@redhat.com
 */
public class RefreshMetaDataCommand extends AbstractShellCommand {

	/**
	 * Constructor.
	 */
	public RefreshMetaDataCommand() {
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.ShellCommand#printUsage()
	 */
	@Override
	public void printUsage() {
		print("s-ramp:refreshMetaData");
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.ShellCommand#printHelp()
	 */
	@Override
	public void printHelp() {
		print("The 'refreshMetaData' command downloads the latest meta-data for");
		print("a single artifact from the S-RAMP repository.  The artifact in");
		print("question is the currently active artifact in the session.  If no");
		print("artifact is currently active, then this command will fail.  This");
		print("essentially re-downloads the meta-data for the current artifact");
		print("and replaces any changes that may have existed there.");
		print("");
		print("Example usage:");
		print(">  s-ramp:refreshMetaData");
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.ShellCommand#execute()
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
			print("No active S-RAMP artifact exists.  Use s-ramp:getMetaData or s-ramp:upload.");
			return;
		}

		try {
			ArtifactType type = ArtifactType.valueOf(artifact);
			BaseArtifactType metaData = client.getArtifactMetaData(type, artifact.getUuid());
			getContext().setVariable(artifactVarName, metaData);
			print("Successfully refreshed meta-data for artifact '%1$s'.", artifact.getName());
			print("Meta Data for: " + artifact.getUuid());
			print("--------------");
			PrintArtifactMetaDataVisitor visitor = new PrintArtifactMetaDataVisitor();
			ArtifactVisitorHelper.visitArtifact(visitor, artifact);
		} catch (Exception e) {
			print("FAILED to update the artifact.");
			print("\t" + e.getMessage());
		}
	}

}
