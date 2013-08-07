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
import java.util.List;

import javax.xml.namespace.QName;

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.overlord.sramp.atom.archive.SrampArchiveJaxbUtils;
import org.overlord.sramp.client.SrampAtomApiClient;
import org.overlord.sramp.client.query.ArtifactSummary;
import org.overlord.sramp.client.query.QueryResultSet;
import org.overlord.sramp.common.visitors.ArtifactVisitorHelper;
import org.overlord.sramp.shell.BuiltInShellCommand;
import org.overlord.sramp.shell.api.InvalidCommandArgumentException;
import org.overlord.sramp.shell.i18n.Messages;
import org.overlord.sramp.shell.util.FileNameCompleter;
import org.overlord.sramp.shell.util.PrintArtifactMetaDataVisitor;

/**
 * Gets the full meta-data for a single artifact in the s-ramp repo.
 *
 * @author eric.wittmann@redhat.com
 */
public class GetMetaDataCommand extends BuiltInShellCommand {

	/**
	 * Constructor.
	 */
	public GetMetaDataCommand() {
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.ShellCommand#execute()
	 */
	@Override
	public void execute() throws Exception {
		String artifactIdArg = this.requiredArgument(0, Messages.i18n.format("InvalidArgMsg.ArtifactId")); //$NON-NLS-1$
		String outputFilePathArg = this.optionalArgument(1);
		if (!artifactIdArg.contains(":")) { //$NON-NLS-1$
            throw new InvalidCommandArgumentException(0, Messages.i18n.format("InvalidArtifactIdFormat")); //$NON-NLS-1$
		}
		QName clientVarName = new QName("s-ramp", "client"); //$NON-NLS-1$ //$NON-NLS-2$
		QName feedVarName = new QName("s-ramp", "feed"); //$NON-NLS-1$ //$NON-NLS-2$
		SrampAtomApiClient client = (SrampAtomApiClient) getContext().getVariable(clientVarName);
		if (client == null) {
			print(Messages.i18n.format("MissingSRAMPConnection")); //$NON-NLS-1$
			return;
		}

		BaseArtifactType artifact = null;
		String idType = artifactIdArg.substring(0, artifactIdArg.indexOf(':'));
		if ("feed".equals(idType)) { //$NON-NLS-1$
			QueryResultSet rset = (QueryResultSet) getContext().getVariable(feedVarName);
			int feedIdx = Integer.parseInt(artifactIdArg.substring(artifactIdArg.indexOf(':')+1)) - 1;
			if (feedIdx < 0 || feedIdx >= rset.size()) {
                throw new InvalidCommandArgumentException(0, Messages.i18n.format("FeedIndexOutOfRange")); //$NON-NLS-1$
			}
			ArtifactSummary summary = rset.get(feedIdx);
			String artifactUUID = summary.getUuid();
			artifact = client.getArtifactMetaData(summary.getType(), artifactUUID);
		} else if ("uuid".equals(idType)) { //$NON-NLS-1$
//			String artifactUUID = artifactIdArg.substring(artifactIdArg.indexOf(':') + 1);
//			artifact = getArtifactMetaDataByUUID(client, artifactUUID);
            throw new InvalidCommandArgumentException(0, Messages.i18n.format("UuidNotImplemented")); //$NON-NLS-1$
		} else {
            throw new InvalidCommandArgumentException(0, Messages.i18n.format("InvalidArtifactIdFormat")); //$NON-NLS-1$
		}

		// Store the artifact in the context, making it the active artifact.
		QName artifactVarName = new QName("s-ramp", "artifact"); //$NON-NLS-1$ //$NON-NLS-2$
		getContext().setVariable(artifactVarName, artifact);

		if (outputFilePathArg == null) {
			// Print out the meta-data information
			print(Messages.i18n.format("GetMetaData.MetaDataLabel", artifact.getUuid())); //$NON-NLS-1$
			print("--------------"); //$NON-NLS-1$
			PrintArtifactMetaDataVisitor visitor = new PrintArtifactMetaDataVisitor();
			ArtifactVisitorHelper.visitArtifact(visitor, artifact);
		} else {
			File outFile = new File(outputFilePathArg);
			if (outFile.isFile()) {
				throw new InvalidCommandArgumentException(1, Messages.i18n.format("GetMetaData.OutputFileExists", outFile.getCanonicalPath())); //$NON-NLS-1$
			} else if (outFile.isDirectory()) {
				String fileName = artifact.getName() + "-metadata.xml"; //$NON-NLS-1$
				outFile = new File(outFile, fileName);
			}
			outFile.getParentFile().mkdirs();
			SrampArchiveJaxbUtils.writeMetaData(outFile, artifact, false);
			print(Messages.i18n.format("GetMetaData.SavedTo", outFile.getCanonicalPath())); //$NON-NLS-1$
		}
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.AbstractShellCommand#tabCompletion(java.lang.String, java.util.List)
	 */
	@Override
	public int tabCompletion(String lastArgument, List<CharSequence> candidates) {
		if (getArguments().isEmpty() && (lastArgument == null || "feed:".startsWith(lastArgument))) { //$NON-NLS-1$
			QName feedVarName = new QName("s-ramp", "feed"); //$NON-NLS-1$ //$NON-NLS-2$
			QueryResultSet rset = (QueryResultSet) getContext().getVariable(feedVarName);
			if (rset != null) {
				for (int idx = 0; idx < rset.size(); idx++) {
					String candidate = "feed:" + (idx+1); //$NON-NLS-1$
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
				lastArgument = ""; //$NON-NLS-1$
			FileNameCompleter delegate = new FileNameCompleter();
			return delegate.complete(lastArgument, lastArgument.length(), candidates);
		} else {
			return -1;
		}
	}

}
