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
package org.overlord.sramp.client.shell.commands.ontology;

import java.io.File;
import java.io.InputStream;

import javax.xml.namespace.QName;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.overlord.sramp.client.SrampAtomApiClient;
import org.overlord.sramp.client.shell.AbstractShellCommand;
import org.overlord.sramp.client.shell.ShellContext;

/**
 * Uploads an ontology (S-RAMP OWL format) to the s-ramp repository.
 *
 * @author eric.wittmann@redhat.com
 */
public class UploadOntologyCommand extends AbstractShellCommand {

	/**
	 * Constructor.
	 */
	public UploadOntologyCommand() {
	}

	/**
	 * @see org.overlord.sramp.client.shell.ShellCommand#printUsage()
	 */
	@Override
	public void printUsage() {
		System.out.println("ontology:upload <pathToOntologyFile>");
	}

	/**
	 * @see org.overlord.sramp.client.shell.ShellCommand#printHelp()
	 */
	@Override
	public void printHelp() {
		System.out.println("The 'upload' command uploads a new OWL ontology file to the");
		System.out.println("S-RAMP repository.  This makes the classes defined in the OWL");
		System.out.println("ontology available for use as classifications on artifacts.");
		System.out.println("");
		System.out.println("Example usage:");
		System.out.println(">  ontology:upload /home/uname/files/regions.owl.xml");
	}

	/**
	 * @see org.overlord.sramp.client.shell.ShellCommand#execute(org.overlord.sramp.client.shell.ShellContext)
	 */
	@Override
	public void execute(ShellContext context) throws Exception {
		String filePathArg = this.requiredArgument(0, "Please specify a path to a local ontology file.");

		QName clientVarName = new QName("s-ramp", "client");
		SrampAtomApiClient client = (SrampAtomApiClient) context.getVariable(clientVarName);
		if (client == null) {
			System.out.println("No S-RAMP repository connection is currently open.");
			return;
		}
		InputStream content = null;
		try {
			File file = new File(filePathArg);
			content = FileUtils.openInputStream(file);
			client.uploadOntology(content);
			System.out.println("Successfully uploaded a new ontology to the S-RAMP repository.");
		} catch (Exception e) {
			System.out.println("FAILED to upload an artifact.");
			System.out.println("\t" + e.getMessage());
			IOUtils.closeQuietly(content);
		} finally {
			IOUtils.closeQuietly(content);
		}
	}
}
