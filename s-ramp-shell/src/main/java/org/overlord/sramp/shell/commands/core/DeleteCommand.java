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

import org.apache.commons.lang.StringUtils;
import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.cl.Option;
import org.jboss.aesh.cl.validator.CommandValidator;
import org.jboss.aesh.cl.validator.CommandValidatorException;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.shell.ShellCommandConstants;
import org.overlord.sramp.shell.i18n.Messages;


/**
 * Deletes an artifact from the S-RAMP repository.
 *
 * @author eric.wittmann@redhat.com
 */
@CommandDefinition(name = ShellCommandConstants.Sramp.S_RAMP_COMMAND_DELETE, validator = DeleteCommand.CustomCommandValidator.class, description = "Deletes an artifact from the S-RAMP repository.")
public class DeleteCommand extends AbstractCoreShellCommand {

    private BaseArtifactType artifact;

    @Option(required = false, name = "feedIndex", hasValue = true, shortName = 'f')
    private Integer _feedIndex;

    @Option(required = false, name = "uuid", hasValue = true, shortName = 'u')
    private String _uuid;

    @Option(overrideRequired = true, name = "help", hasValue = false, shortName = 'h')
    private boolean _help;

	/**
	 * Constructor.
	 */
	public DeleteCommand() {

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
	            return false;
        }
        artifact = getArtifact(_feedIndex, _uuid);

        if (artifact == null) {
            artifact = getArtifact();
            if (artifact == null) {
                print(Messages.i18n.format("NoActiveArtifact")); //$NON-NLS-1$
                return false;
            }
        }

		try {
			client.deleteArtifact(artifact.getUuid(), ArtifactType.valueOf(artifact));
			print(Messages.i18n.format("Delete.Success", artifact.getName())); //$NON-NLS-1$
		} catch (Exception e) {
			print(Messages.i18n.format("Delete.Failure")); //$NON-NLS-1$
			print("\t" + e.getMessage()); //$NON-NLS-1$
	        return false;
		}
        return true;
	}



    /* (non-Javadoc)
     * @see org.overlord.sramp.shell.BuiltInShellCommand#getName()
     */
    @Override
    public String getName() {
        return ShellCommandConstants.Sramp.S_RAMP_COMMAND_DELETE;
    }


    /**
     * Validates that the Delete command is properly filled.
     * 
     * @author David Virgil Naranjo
     */
    public class CustomCommandValidator implements CommandValidator<DeleteCommand> {

        /**
         * Instantiates a new custom command validator.
         */
        public CustomCommandValidator() {

        }

        /* (non-Javadoc)
         * @see org.jboss.aesh.cl.validator.CommandValidator#validate(org.jboss.aesh.console.command.Command)
         */
        @Override
        public void validate(DeleteCommand command) throws CommandValidatorException {
            if (StringUtils.isBlank(command.getUuid()) && command.getFeedIndex() == null) {
                throw new CommandValidatorException(Messages.i18n.format("Artifact.feed.no.option.selected"));

            } else if (!StringUtils.isBlank(command.getUuid()) && command.getFeedIndex() != null) {
                throw new CommandValidatorException(
                        Messages.i18n.format("Artifact.feed.both.option.selected"));
            }

        }
    }



    /* (non-Javadoc)
     * @see org.overlord.sramp.shell.commands.core.AbstractCoreShellCommand#getArtifact()
     */
    @Override
    public BaseArtifactType getArtifact() {
        return artifact;
    }

    /**
     * Sets the artifact.
     *
     * @param artifact
     *            the new artifact
     */
    public void setArtifact(BaseArtifactType artifact) {
        this.artifact = artifact;
    }

    /**
     * Gets the feed index.
     *
     * @return the feed index
     */
    public Integer getFeedIndex() {
        return _feedIndex;
    }

    /**
     * Sets the feed index.
     *
     * @param feedIndex
     *            the new feed index
     */
    public void setFeedIndex(Integer feedIndex) {
        this._feedIndex = feedIndex;
    }

    /**
     * Gets the uuid.
     *
     * @return the uuid
     */
    public String getUuid() {
        return _uuid;
    }

    /**
     * Sets the uuid.
     *
     * @param uuid
     *            the new uuid
     */
    public void setUuid(String uuid) {
        this._uuid = uuid;
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
