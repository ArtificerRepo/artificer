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

import javax.xml.namespace.QName;

import org.artificer.shell.BuiltInShellCommand;
import org.artificer.shell.i18n.Messages;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.artificer.client.ArtificerAtomApiClient;

/**
 * Updates an artifact's meta-data in the s-ramp repository. This requires an active artifact to exist in the
 * context, which was presumably modified in some way (updated core meta-data, properties, relationships,
 * etc).
 *
 * @author eric.wittmann@redhat.com
 */
public class UpdateMetaDataCommand extends BuiltInShellCommand {

	/**
	 * Constructor.
	 */
	public UpdateMetaDataCommand() {
	}

	@Override
	public boolean execute() throws Exception {
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

		try {
			client.updateArtifactMetaData(artifact);
			print(Messages.i18n.format("UpdateMetaData.Success", artifact.getName())); //$NON-NLS-1$
		} catch (Exception e) {
			print(Messages.i18n.format("UpdateMetaData.Failure")); //$NON-NLS-1$
			print("\t" + e.getMessage()); //$NON-NLS-1$
		}
        return true;
	}

}
