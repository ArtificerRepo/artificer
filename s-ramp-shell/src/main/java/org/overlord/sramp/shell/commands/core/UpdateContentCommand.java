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

import java.io.File;
import java.io.InputStream;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.overlord.sramp.client.SrampAtomApiClient;
import org.overlord.sramp.shell.api.AbstractShellCommand;
import org.overlord.sramp.shell.api.InvalidCommandArgumentException;
import org.overlord.sramp.shell.util.FileNameCompleter;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;

/**
 * Updates an artifact's content in the s-ramp repository. This requires an active artifact to exist in the
 * context.
 *
 * @author eric.wittmann@redhat.com
 */
public class UpdateContentCommand extends AbstractShellCommand {

	/**
	 * Constructor.
	 */
	public UpdateContentCommand() {
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.ShellCommand#printUsage()
	 */
	@Override
	public void printUsage() {
		print("s-ramp:updateContent <filePathToContent>");
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.ShellCommand#printHelp()
	 */
	@Override
	public void printHelp() {
		print("The 'updateContent' command updates the content of the currently active");
		print("artifact in the context.  The new content is uploaded to the S-RAMP");
		print("server.");
		print("");
		print("Example usage:");
		print(">  s-ramp:updateContent /home/uname/files/new-content.wsdl");
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.ShellCommand#execute()
	 */
	@Override
	public void execute() throws Exception {
		String contentFilePathArg = requiredArgument(0, "Please supply a file path to the new content.");
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

		File file = new File(contentFilePathArg);
		if (!file.isFile()) {
			throw new InvalidCommandArgumentException(0, "Please supply a path to a valid content file.");
		}

		InputStream content = null;
		try {
			content = FileUtils.openInputStream(file);
			client.updateArtifactContent(artifact, content);
			print("Successfully updated artifact %1$s.", artifact.getName());
		} catch (Exception e) {
			print("FAILED to update the artifact.");
			print("\t" + e.getMessage());
			IOUtils.closeQuietly(content);
		}
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.AbstractShellCommand#tabCompletion(java.lang.String, java.util.List)
	 */
	@Override
	public int tabCompletion(String lastArgument, List<CharSequence> candidates) {
		if (getArguments().isEmpty()) {
			if (lastArgument == null)
				lastArgument = "";
			FileNameCompleter delegate = new FileNameCompleter();
			return delegate.complete(lastArgument, lastArgument.length(), candidates);
		}
		return -1;
	}

}
