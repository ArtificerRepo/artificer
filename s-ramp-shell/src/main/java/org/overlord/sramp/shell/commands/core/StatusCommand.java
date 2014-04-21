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

import javax.xml.namespace.QName;

import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.cl.Option;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.overlord.sramp.client.query.QueryResultSet;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.shell.ShellCommandConstants;
import org.overlord.sramp.shell.i18n.Messages;


/**
 * Displays a summary of the current status, including what repository the
 * user is currently connected to (if any).
 *
 * @author eric.wittmann@redhat.com
 */
@CommandDefinition(name = ShellCommandConstants.Sramp.S_RAMP_COMMAND_STATUS, description = "Displays a summary of the current status")
public class StatusCommand extends AbstractCoreShellCommand {

    @Option(overrideRequired = true, name = "help", hasValue = false, shortName = 'h')
    private boolean _help;

	/**
	 * Constructor.
	 */
	public StatusCommand() {
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

        QName artifactVarName = new QName("s-ramp", "artifact"); //$NON-NLS-1$ //$NON-NLS-2$
		QName feedVarName = new QName("s-ramp", "feed"); //$NON-NLS-1$ //$NON-NLS-2$


		BaseArtifactType artifact = (BaseArtifactType) getContext().getVariable(artifactVarName);
		QueryResultSet feed = (QueryResultSet) getContext().getVariable(feedVarName);

		if (client == null) {
			print (Messages.i18n.format("Status.Status1")); //$NON-NLS-1$
		} else {
			print (Messages.i18n.format("Status.Status2", client.getEndpoint())); //$NON-NLS-1$
		}

		if (artifact == null) {
			print(Messages.i18n.format("Status.Status3")); //$NON-NLS-1$
		} else {
			ArtifactType type = ArtifactType.valueOf(artifact);
			print(Messages.i18n.format("Status.Status4", artifact.getName(), type.getType())); //$NON-NLS-1$
		}

		if (feed == null) {
			print(Messages.i18n.format("Status.Status5")); //$NON-NLS-1$
		} else {
			print(Messages.i18n.format("Status.Status6", feed.size())); //$NON-NLS-1$
		}
        return true;
	}

    /* (non-Javadoc)
     * @see org.overlord.sramp.shell.BuiltInShellCommand#getName()
     */
    @Override
    public String getName() {
        return ShellCommandConstants.Sramp.S_RAMP_COMMAND_STATUS;
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
