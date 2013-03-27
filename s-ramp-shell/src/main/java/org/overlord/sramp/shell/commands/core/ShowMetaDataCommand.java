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

import org.overlord.sramp.common.visitors.ArtifactVisitorHelper;
import org.overlord.sramp.shell.api.AbstractShellCommand;
import org.overlord.sramp.shell.util.PrintArtifactMetaDataVisitor;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;

/**
 * Shows the full meta-data for the artifact currently active in the
 * session/context.
 *
 * @author eric.wittmann@redhat.com
 */
public class ShowMetaDataCommand extends AbstractShellCommand {

	/**
	 * Constructor.
	 */
	public ShowMetaDataCommand() {
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.ShellCommand#printUsage()
	 */
	@Override
	public void printUsage() {
		print("s-ramp:showMetaData");
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.ShellCommand#printHelp()
	 */
	@Override
	public void printHelp() {
		print("The 'showMetaData' command prints out the meta-data for the");
		print("artifact currently active in the session.");
		print("");
		print("Example usage:");
		print(">  s-ramp:showMetaData");
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.ShellCommand#execute()
	 */
	@Override
	public void execute() throws Exception {
		QName artifactVarName = new QName("s-ramp", "artifact");
		BaseArtifactType artifact = (BaseArtifactType) getContext().getVariable(artifactVarName);
		if (artifact == null) {
			print("No active S-RAMP artifact exists.  Use s-ramp:getMetaData.");
			return;
		}

		// Print out the meta-data information
		print("Meta Data for: " + artifact.getUuid());
		print("--------------");
		PrintArtifactMetaDataVisitor visitor = new PrintArtifactMetaDataVisitor();
		ArtifactVisitorHelper.visitArtifact(visitor, artifact);
	}

}
