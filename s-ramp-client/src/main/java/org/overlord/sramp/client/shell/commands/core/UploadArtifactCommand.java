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
package org.overlord.sramp.client.shell.commands.core;

import java.io.File;
import java.io.InputStream;

import javax.xml.namespace.QName;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.overlord.sramp.ArtifactType;
import org.overlord.sramp.client.SrampAtomApiClient;
import org.overlord.sramp.client.shell.AbstractShellCommand;
import org.overlord.sramp.client.shell.ShellContext;
import org.overlord.sramp.client.shell.util.PrintArtifactMetaDataVisitor;
import org.overlord.sramp.visitors.ArtifactVisitorHelper;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;

/**
 * Uploads an artifact to the s-ramp repository.
 *
 * @author eric.wittmann@redhat.com
 */
public class UploadArtifactCommand extends AbstractShellCommand {

	/**
	 * Constructor.
	 */
	public UploadArtifactCommand() {
	}

	/**
	 * @see org.overlord.sramp.client.shell.ShellCommand#printUsage()
	 */
	@Override
	public void printUsage() {
		System.out.println("s-ramp:uploadArtifact <pathToArtifactContent> [<artifactType>]");
	}

	/**
	 * @see org.overlord.sramp.client.shell.ShellCommand#printHelp()
	 */
	@Override
	public void printHelp() {
		System.out.println("The 'uploadArtifact' command uploads the content of a local");
		System.out.println("file to the S-RAMP repository, creating a new artifact.  The");
		System.out.println("artifact type can optionally be provided.  If excluded, the");
		System.out.println("artifact type will be determined based on file extension.");
		System.out.println("");
		System.out.println("Example usages:");
		System.out.println(">  s-ramp:uploadArtifact /home/uname/files/mytypes.xsd");
		System.out.println(">  s-ramp:uploadArtifact /home/uname/files/myservice.wsdl WsdlDocument");
	}

	/**
	 * @see org.overlord.sramp.client.shell.ShellCommand#execute(org.overlord.sramp.client.shell.ShellContext)
	 */
	@Override
	public void execute(ShellContext context) throws Exception {
		String filePathArg = this.requiredArgument(0, "Please specify a path to a local file.");
		String artifactTypeArg = this.optionalArgument(1);

		QName clientVarName = new QName("s-ramp", "client");
		SrampAtomApiClient client = (SrampAtomApiClient) context.getVariable(clientVarName);
		InputStream content = null;
		try {
			File file = new File(filePathArg);
			ArtifactType artifactType = null;
			if (artifactTypeArg != null) {
				artifactType = ArtifactType.valueOf(artifactTypeArg);
			} else {
				artifactType = determineArtifactType(file);
			}
			content = FileUtils.openInputStream(file);
			BaseArtifactType artifact = client.uploadArtifact(artifactType, content, file.getName());
			System.out.println("Successfully uploaded an artifact.");
			PrintArtifactMetaDataVisitor visitor = new PrintArtifactMetaDataVisitor();
			ArtifactVisitorHelper.visitArtifact(visitor, artifact);
		} catch (Exception e) {
			System.out.println("FAILED to upload an artifact.");
			System.out.println("\t" + e.getMessage());
			IOUtils.closeQuietly(content);
		}
	}

	/**
	 * Try to figure out what kind of artifact we're dealing with.
	 * @param file
	 */
	private ArtifactType determineArtifactType(File file) {
		ArtifactType type = null;
		String extension = FilenameUtils.getExtension(file.getName());
		if ("wsdl".equals(extension)) {
			type = ArtifactType.WsdlDocument;
		} else if ("xsd".equals(extension)) {
			type = ArtifactType.XsdDocument;
		} else if ("wspolicy".equals(extension)) {
			type = ArtifactType.PolicyDocument;
		} else if ("xml".equals(extension)) {
			type = ArtifactType.XmlDocument;
		} else {
			type = ArtifactType.valueOf("Document");
		}
		return type;
	}

}
