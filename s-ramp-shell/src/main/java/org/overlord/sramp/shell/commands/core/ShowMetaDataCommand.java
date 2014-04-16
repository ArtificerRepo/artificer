/*
 * Copyright 2014 JBoss Inc
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

import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.cl.Option;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.overlord.sramp.common.visitors.ArtifactVisitorHelper;
import org.overlord.sramp.shell.ShellCommandConstants;
import org.overlord.sramp.shell.i18n.Messages;
import org.overlord.sramp.shell.util.PrintArtifactMetaDataVisitor;


/**
 * Shows the full meta-data for the artifact currently active in the
 * session/context.
 *
 * @author eric.wittmann@redhat.com
 */
@CommandDefinition(name = ShellCommandConstants.Sramp.S_RAMP_COMMAND_SHOW_METADATA, description = "Shows the full meta-data for the artifact currently active in the session")
public class ShowMetaDataCommand extends AbstractCoreShellCommand {

    @Option(overrideRequired = true, name = "help", hasValue = false, shortName = 'h')
    private boolean _help;

	/**
	 * Constructor.
	 */
	public ShowMetaDataCommand() {
	}

	/**
     * Execute.
     *
     * @return true, if successful
     * @throws Exception
     *             the exception
     * @see org.overlord.sramp.shell.api.shell.ShellCommand#execute()
     */
	@Override
	public boolean execute() throws Exception {
        super.execute();

        BaseArtifactType artifact = getArtifact();
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

    /* (non-Javadoc)
     * @see org.overlord.sramp.shell.BuiltInShellCommand#getName()
     */
    @Override
    public String getName() {
        return ShellCommandConstants.Sramp.S_RAMP_COMMAND_SHOW_METADATA;
    }

    /* (non-Javadoc)
     * @see org.overlord.sramp.shell.BuiltInShellCommand#isHelp()
     */
    @Override
    public boolean isHelp() {
        return _help;
    }

    /**
     * Sets the help.
     *
     * @param help
     *            the new help
     */
    public void setHelp(boolean help) {
        this._help = help;
    }

}
