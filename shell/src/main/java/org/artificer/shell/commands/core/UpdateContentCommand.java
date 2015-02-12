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
package org.artificer.shell.commands.core;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.artificer.shell.BuiltInShellCommand;
import org.artificer.shell.i18n.Messages;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.artificer.client.ArtificerAtomApiClient;
import org.artificer.shell.api.InvalidCommandArgumentException;
import org.artificer.shell.util.FileNameCompleter;

/**
 * Updates an artifact's content in the s-ramp repository. This requires an active artifact to exist in the
 * context.
 *
 * @author eric.wittmann@redhat.com
 */
public class UpdateContentCommand extends BuiltInShellCommand {

	/**
	 * Constructor.
	 */
	public UpdateContentCommand() {
	}

	@Override
	public boolean execute() throws Exception {
		String contentFilePathArg = requiredArgument(0, Messages.i18n.format("UpdateContent.InvalidArgMsg.PathToContent")); //$NON-NLS-1$
		QName clientVarName = new QName("s-ramp", "client"); //$NON-NLS-1$ //$NON-NLS-2$
		QName artifactVarName = new QName("s-ramp", "artifact"); //$NON-NLS-1$ //$NON-NLS-2$

		ArtificerAtomApiClient client = (ArtificerAtomApiClient) getContext().getVariable(clientVarName);
		if (client == null) {
			print(Messages.i18n.format("MissingArtificerConnection")); //$NON-NLS-1$
			return false;
		}

		BaseArtifactType artifact = (BaseArtifactType) getContext().getVariable(artifactVarName);
		if (artifact == null) {
			print(Messages.i18n.format("NoActiveArtifact")); //$NON-NLS-1$
			return false;
		}

		File file = new File(contentFilePathArg);
		if (!file.isFile()) {
			throw new InvalidCommandArgumentException(0, Messages.i18n.format("UpdateContent.InvalidArgMsg.PathToFile")); //$NON-NLS-1$
		}

		InputStream content = null;
		try {
			content = FileUtils.openInputStream(file);
			client.updateArtifactContent(artifact, content);
			print(Messages.i18n.format("UpdateContent.Success", artifact.getName())); //$NON-NLS-1$
		} catch (Exception e) {
			print(Messages.i18n.format("UpdateContent.Failure")); //$NON-NLS-1$
			print("\t" + e.getMessage()); //$NON-NLS-1$
			IOUtils.closeQuietly(content);
            return false;
		}
        return true;
	}

	@Override
	public int tabCompletion(String lastArgument, List<CharSequence> candidates) {
		if (getArguments().isEmpty()) {
			if (lastArgument == null)
				lastArgument = ""; //$NON-NLS-1$
			FileNameCompleter delegate = new FileNameCompleter();
			return delegate.complete(lastArgument, lastArgument.length(), candidates);
		}
		return -1;
	}

}
