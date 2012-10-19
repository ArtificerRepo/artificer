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
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.namespace.QName;

import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.jboss.resteasy.plugins.providers.atom.Feed;
import org.overlord.sramp.ArtifactType;
import org.overlord.sramp.atom.SrampAtomUtils;
import org.overlord.sramp.client.SrampAtomApiClient;
import org.overlord.sramp.client.shell.AbstractShellCommand;
import org.overlord.sramp.client.shell.ShellContext;
import org.overlord.sramp.client.shell.commands.InvalidCommandArgumentException;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;

/**
 * Gets the content for a single artifact in the s-ramp repo.
 *
 * @author eric.wittmann@redhat.com
 */
public class GetContentCommand extends AbstractShellCommand {

	/**
	 * Constructor.
	 */
	public GetContentCommand() {
	}

	/**
	 * @see org.overlord.sramp.client.shell.ShellCommand#printUsage()
	 */
	@Override
	public void printUsage() {
		System.out.println("s-ramp:getContent <artifactId> <outputFilePath>");
		System.out.println("\tValid formats for artifactId:");
		System.out.println("\t  feed:<feedIndex> - an index into the most recent feed");
		System.out.println("\t  uuid:<srampUUID> - the UUID of an s-ramp artifact");
	}

	/**
	 * @see org.overlord.sramp.client.shell.ShellCommand#printHelp()
	 */
	@Override
	public void printHelp() {
		System.out.println("The 'getContent' command downloads the file content for");
		System.out.println("a single artifact from the S-RAMP repository.  The artifact");
		System.out.println("can be identified either by its unique S-RAMP uuid or else");
		System.out.println("by an index into the most recent Feed.");
		System.out.println("");
		System.out.println("Note: a Feed can be obtained, for example, by using the ");
		System.out.println("s-ramp:query command.");
		System.out.println("");
		System.out.println("Example usage:");
		System.out.println(">  s-ramp:query /s-ramp/wsdl/WsdlDocument");
		System.out.println(">  s-ramp:getContent feed:1 /home/user/files/");
	}

	/**
	 * @see org.overlord.sramp.client.shell.ShellCommand#execute(org.overlord.sramp.client.shell.ShellContext)
	 */
	@Override
	public void execute(ShellContext context) throws Exception {
		String artifactIdArg = this.requiredArgument(0, "Please specify a valid artifact identifier.");
		String outputFilePathArg = this.requiredArgument(1, "Please specify an output path (file or directory).");
		if (!artifactIdArg.contains(":")) {
			throw new InvalidCommandArgumentException(0, "Invalid artifact id format.");
		}
		QName clientVarName = new QName("s-ramp", "client");
		QName feedVarName = new QName("s-ramp", "feed");
		SrampAtomApiClient client = (SrampAtomApiClient) context.getVariable(clientVarName);

		BaseArtifactType artifact = null;
		String idType = artifactIdArg.substring(0, artifactIdArg.indexOf(':'));
		if ("feed".equals(idType)) {
			Feed feed = (Feed) context.getVariable(feedVarName);
			context.setVariable(feedVarName, feed);
			int feedIdx = Integer.parseInt(artifactIdArg.substring(artifactIdArg.indexOf(':')+1)) - 1;
			if (feedIdx < 0 || feedIdx >= feed.getEntries().size()) {
				throw new InvalidCommandArgumentException(0, "Feed index out of range.");
			}
			Entry entry = feed.getEntries().get(feedIdx);
			String artifactUUID = entry.getId().toString();
			Entry fullArtifactEntry = client.getFullArtifactEntry(SrampAtomUtils.getArtifactType(entry), artifactUUID);
			artifact = SrampAtomUtils.unwrapSrampArtifact(fullArtifactEntry);
		} else if ("uuid".equals(idType)) {
//			String artifactUUID = artifactIdArg.substring(artifactIdArg.indexOf(':') + 1);
//			artifact = getArtifactMetaDataByUUID(client, artifactUUID);
			throw new InvalidCommandArgumentException(0, "uuid: style artifact identifiers not yet implemented.");
		} else {
			throw new InvalidCommandArgumentException(0, "Invalid artifact id format.");
		}

		File outFile = new File(outputFilePathArg);
		if (outFile.isFile()) {
			throw new InvalidCommandArgumentException(1, "Output file already exists: " + outFile.getCanonicalPath());
		} else if (outFile.isDirectory()) {
			String fileName = artifact.getName();
			outFile = new File(outFile, fileName);
		}
		outFile.getParentFile().mkdirs();

		InputStream artifactContent = null;
		OutputStream outputStream = null;

		try {
			artifactContent = client.getArtifactContent(ArtifactType.valueOf(artifact), artifact.getUuid());
			outputStream = new FileOutputStream(outFile);
			IOUtils.copy(artifactContent, outputStream);
			System.out.println("Artifact content saved to " + outFile.getCanonicalPath());
		} finally {
			IOUtils.closeQuietly(artifactContent);
			IOUtils.closeQuietly(outputStream);
		}
	}

}
