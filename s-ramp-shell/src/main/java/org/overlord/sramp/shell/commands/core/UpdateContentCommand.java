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

import java.io.File;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.cl.Option;
import org.jboss.aesh.cl.completer.FileOptionCompleter;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.overlord.sramp.shell.ShellCommandConstants;
import org.overlord.sramp.shell.i18n.Messages;


/**
 * Updates an artifact's content in the s-ramp repository. This requires an active artifact to exist in the
 * context.
 *
 * @author eric.wittmann@redhat.com
 */
@CommandDefinition(name = ShellCommandConstants.Sramp.S_RAMP_COMMAND_UPDATE_CONTENT, description = "Updates an artifact's content in the s-ramp repository")
public class UpdateContentCommand extends AbstractCoreShellCommand {

    @Option(hasValue = true, required = true, name = "file", shortName = 'f', completer = FileOptionCompleter.class)
    File _file;

    @Option(overrideRequired = true, name = "help", hasValue = false, shortName = 'h')
    private boolean _help;

    /**
     * Constructor.
     */
	public UpdateContentCommand() {
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

        if (client == null) {
            print(Messages.i18n.format("MissingSRAMPConnection")); //$NON-NLS-1$
        }

        BaseArtifactType artifact = getArtifact();
        if (artifact == null) {
            print(Messages.i18n.format("NoActiveArtifact")); //$NON-NLS-1$
            return false;
        }



		InputStream content = null;
		try {
			content = FileUtils.openInputStream(_file);
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




    /* (non-Javadoc)
     * @see org.overlord.sramp.shell.BuiltInShellCommand#getName()
     */
    @Override
    public String getName() {
        return ShellCommandConstants.Sramp.S_RAMP_COMMAND_UPDATE_CONTENT;
    }


    /**
     * Gets the file.
     *
     * @return the file
     */
    public File getFile() {
        return _file;
    }

    /**
     * Sets the file.
     *
     * @param file
     *            the new file
     */
    public void setFile(File file) {
        this._file = file;
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
