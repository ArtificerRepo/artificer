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
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.commons.io.IOUtils;
import org.overlord.sramp.client.SrampAtomApiClient;
import org.overlord.sramp.client.query.ArtifactSummary;
import org.overlord.sramp.client.query.QueryResultSet;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.shell.api.AbstractShellCommand;
import org.overlord.sramp.shell.api.InvalidCommandArgumentException;
import org.overlord.sramp.shell.util.FileNameCompleter;
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
	 * @see org.overlord.sramp.shell.api.shell.ShellCommand#printUsage()
	 */
	@Override
	public void printUsage() {
		print("s-ramp:getContent <artifactId> <outputFilePath>");
		print("\tValid formats for artifactId:");
		print("\t  feed:<feedIndex> - an index into the most recent feed");
		print("\t  uuid:<srampUUID> - the UUID of an s-ramp artifact");
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.ShellCommand#printHelp()
	 */
	@Override
	public void printHelp() {
		print("The 'getContent' command downloads the file content for");
		print("a single artifact from the S-RAMP repository.  The artifact");
		print("can be identified either by its unique S-RAMP uuid or else");
		print("by an index into the most recent Feed.");
		print("");
		print("Note: a Feed can be obtained, for example, by using the ");
		print("s-ramp:query command.");
		print("");
		print("Example usage:");
		print(">  s-ramp:query /s-ramp/wsdl/WsdlDocument");
		print(">  s-ramp:getContent feed:1 /home/user/files/");
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.ShellCommand#execute()
	 */
	@Override
	public void execute() throws Exception {
		String artifactIdArg = this.requiredArgument(0, "Please specify a valid artifact identifier.");
		String outputFilePathArg = this.requiredArgument(1, "Please specify an output path (file or directory).");
		if (!artifactIdArg.contains(":")) {
			throw new InvalidCommandArgumentException(0, "Invalid artifact id format.");
		}
		QName clientVarName = new QName("s-ramp", "client");
		QName feedVarName = new QName("s-ramp", "feed");
		SrampAtomApiClient client = (SrampAtomApiClient) getContext().getVariable(clientVarName);
		if (client == null) {
			print("No S-RAMP repository connection is currently open.");
			return;
		}

		BaseArtifactType artifact = null;
		String idType = artifactIdArg.substring(0, artifactIdArg.indexOf(':'));
		if ("feed".equals(idType)) {
			QueryResultSet rset = (QueryResultSet) getContext().getVariable(feedVarName);
			int feedIdx = Integer.parseInt(artifactIdArg.substring(artifactIdArg.indexOf(':')+1)) - 1;
			if (feedIdx < 0 || feedIdx >= rset.size()) {
				throw new InvalidCommandArgumentException(0, "Feed index out of range.");
			}
			ArtifactSummary summary = rset.get(feedIdx);
			String artifactUUID = summary.getUuid();
			artifact = client.getArtifactMetaData(summary.getType(), artifactUUID);
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
			print("Artifact content saved to " + outFile.getCanonicalPath());
		} finally {
			IOUtils.closeQuietly(artifactContent);
			IOUtils.closeQuietly(outputStream);
		}
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.AbstractShellCommand#tabCompletion(java.lang.String, java.util.List)
	 */
	@Override
	public int tabCompletion(String lastArgument, List<CharSequence> candidates) {
		if (getArguments().isEmpty() && (lastArgument == null || "feed:".startsWith(lastArgument))) {
			QName feedVarName = new QName("s-ramp", "feed");
			QueryResultSet rset = (QueryResultSet) getContext().getVariable(feedVarName);
			if (rset != null) {
				for (int idx = 0; idx < rset.size(); idx++) {
					String candidate = "feed:" + (idx+1);
					if (lastArgument == null) {
						candidates.add(candidate);
					}
					if (lastArgument != null && candidate.startsWith(lastArgument)) {
						candidates.add(candidate);
					}
				}
			}
			return 0;
		} else if (getArguments().size() == 1) {
			if (lastArgument == null)
				lastArgument = "";
			FileNameCompleter delegate = new FileNameCompleter();
			return delegate.complete(lastArgument, lastArgument.length(), candidates);
		} else {
			return -1;
		}
	}

}
