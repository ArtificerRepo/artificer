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
import org.artificer.shell.util.PrintArtifactMetaDataVisitor;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.artificer.common.visitors.ArtifactVisitorHelper;

/**
 * Shows the full meta-data for the artifact currently active in the
 * session/context.
 *
 * @author eric.wittmann@redhat.com
 */
public class ShowMetaDataCommand extends BuiltInShellCommand {

	/**
	 * Constructor.
	 */
	public ShowMetaDataCommand() {
	}

	@Override
	public boolean execute() throws Exception {
		QName artifactVarName = new QName("s-ramp", "artifact"); //$NON-NLS-1$ //$NON-NLS-2$
		BaseArtifactType artifact = (BaseArtifactType) getContext().getVariable(artifactVarName);
		if (artifact == null) {
			print(Messages.i18n.format("NoActiveArtifact")); //$NON-NLS-1$
			return false;
		}

		// Print out the meta-data information
		print(Messages.i18n.format("RefreshMetaData.MetaDataFor", artifact.getUuid())); //$NON-NLS-1$
		print("--------------"); //$NON-NLS-1$
		PrintArtifactMetaDataVisitor visitor = new PrintArtifactMetaDataVisitor();
		ArtifactVisitorHelper.visitArtifact(visitor, artifact);
        return true;
	}

}
