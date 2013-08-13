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

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.overlord.sramp.client.SrampAtomApiClient;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.common.visitors.ArtifactVisitorHelper;
import org.overlord.sramp.shell.BuiltInShellCommand;
import org.overlord.sramp.shell.i18n.Messages;
import org.overlord.sramp.shell.util.PrintArtifactMetaDataVisitor;

/**
 * Refreshes the full meta-data for a single artifact - namely the currently active
 * artifact in the session.
 *
 * @author eric.wittmann@redhat.com
 */
public class RefreshMetaDataCommand extends BuiltInShellCommand {

	/**
	 * Constructor.
	 */
	public RefreshMetaDataCommand() {
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.ShellCommand#execute()
	 */
	@Override
	public boolean execute() throws Exception {
		QName clientVarName = new QName("s-ramp", "client"); //$NON-NLS-1$ //$NON-NLS-2$
		QName artifactVarName = new QName("s-ramp", "artifact"); //$NON-NLS-1$ //$NON-NLS-2$

		SrampAtomApiClient client = (SrampAtomApiClient) getContext().getVariable(clientVarName);
		if (client == null) {
			print(Messages.i18n.format("MissingSRAMPConnection")); //$NON-NLS-1$
            return false;
		}

		BaseArtifactType artifact = (BaseArtifactType) getContext().getVariable(artifactVarName);
		if (artifact == null) {
            print(Messages.i18n.format("NoActiveArtifact")); //$NON-NLS-1$
            return false;
		}

		try {
			ArtifactType type = ArtifactType.valueOf(artifact);
			BaseArtifactType metaData = client.getArtifactMetaData(type, artifact.getUuid());
			getContext().setVariable(artifactVarName, metaData);
			print(Messages.i18n.format("RefreshMetaData.Success", artifact.getName())); //$NON-NLS-1$
			print(Messages.i18n.format("RefreshMetaData.MetaDataFor", artifact.getUuid())); //$NON-NLS-1$
			print("--------------"); //$NON-NLS-1$
			PrintArtifactMetaDataVisitor visitor = new PrintArtifactMetaDataVisitor();
			ArtifactVisitorHelper.visitArtifact(visitor, artifact);
		} catch (Exception e) {
			print(Messages.i18n.format("RefreshMetaData.Failure")); //$NON-NLS-1$
			print("\t" + e.getMessage()); //$NON-NLS-1$
            return false;
		}
        return true;
	}

}
