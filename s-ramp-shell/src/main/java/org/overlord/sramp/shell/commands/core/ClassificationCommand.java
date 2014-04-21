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
import org.overlord.sramp.shell.ShellCommandConstants;
import org.overlord.sramp.shell.aesh.RequiredOptionRenderer;
import org.overlord.sramp.shell.i18n.Messages;


/**
 * Command used to manipulate classifications on the currently active S-RAMP artifact.  This command
 * can add and remove classifications.
 *
 * @author eric.wittmann@redhat.com
 */
@CommandDefinition(name = ShellCommandConstants.Sramp.S_RAMP_COMMAND_CLASSIFICATION, validator = ClassificationCommand.ClassificationCommandValidator.class, description = "Command used to manipulate classifications on the currently active S-RAMP artifact.")
public class ClassificationCommand extends AbstractCoreShellCommand {



    @Option(required = true, name = "classification", hasValue = true, shortName = 'c', renderer = RequiredOptionRenderer.class)
    private String _classification;


    @Option(hasValue = false, name = "add", shortName = 'a')
    private boolean _add;

    @Option(hasValue = false, name = "remove", shortName = 'r')
    private boolean _remove;

    @Option(hasValue = false, name = "clear", shortName = 'C')
    private boolean _clear;

    @Option(overrideRequired = true, name = "help", hasValue = false, shortName = 'h')
    private boolean _help;

	/**
	 * Constructor.
	 */
	public ClassificationCommand() {
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

		try {
            if (_add) { //$NON-NLS-1$
                artifact.getClassifiedBy().add(_classification);
                print(Messages.i18n.format("Classification.ClassificationAdded", _classification)); //$NON-NLS-1$
            } else if (_remove) { //$NON-NLS-1$
                if (artifact.getClassifiedBy().remove(_classification)) {
                    print(Messages.i18n.format("Classification.ClassificationRemoved"), _classification); //$NON-NLS-1$
				} else {
                    print(Messages.i18n.format("Classification.ClassificationDoesNotExist"), _classification); //$NON-NLS-1$
					return false;
				}
            } else if (_clear) { //$NON-NLS-1$
				if (!artifact.getClassifiedBy().isEmpty()) {
					artifact.getClassifiedBy().clear();
					print(Messages.i18n.format("Classification.AllRemoved")); //$NON-NLS-1$
				} else {
					print(Messages.i18n.format("Classification.NoneExist")); //$NON-NLS-1$
					return false;
				}
			}
        } catch (Exception e) {
			print(Messages.i18n.format("Classification.ModificationFailed")); //$NON-NLS-1$
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
        return ShellCommandConstants.Sramp.S_RAMP_COMMAND_CLASSIFICATION;
    }

    /**
     * Validates that the Classification command is properly filled.
     *
     * @author David Virgil Naranjo
     */
    public class ClassificationCommandValidator implements CommandValidator<ClassificationCommand> {

        /**
         * Instantiates a new classification command validator.
         */
        public ClassificationCommandValidator() {

        }

        /* (non-Javadoc)
         * @see org.jboss.aesh.cl.validator.CommandValidator#validate(org.jboss.aesh.console.command.Command)
         */
        @Override
        public void validate(ClassificationCommand command) throws CommandValidatorException {
            if (!_add && !_remove && !_clear) {
                throw new CommandValidatorException(
                        Messages.i18n.format("Classification.InvalidArgMsg.No.Subcommand"));
            } else if ((_add && _remove) || (_add && _clear) || (_remove && _clear)) {
                throw new CommandValidatorException(
                        Messages.i18n.format("Classification.InvalidArgMsg.Only.One.Action"));
            }

            if (_add || _remove) {
                if (StringUtils.isBlank(command._classification)) {
                    throw new CommandValidatorException(
                            Messages.i18n.format("Classification.InvalidArgMsg.ClassificationUri"));
                }
            }


        }
    }

    /**
     * Gets the classification.
     *
     * @return the classification
     */
    public String getClassification() {
        return _classification;
    }

    /**
     * Sets the classification.
     *
     * @param classification
     *            the new classification
     */
    public void setClassification(String classification) {
        this._classification = classification;
    }

    /**
     * Checks if is adds the.
     *
     * @return true, if is adds the
     */
    public boolean isAdd() {
        return _add;
    }

    /**
     * Sets the adds the.
     *
     * @param add
     *            the new adds the
     */
    public void setAdd(boolean add) {
        this._add = add;
    }

    /**
     * Checks if is removes the.
     *
     * @return true, if is removes the
     */
    public boolean isRemove() {
        return _remove;
    }

    /**
     * Sets the removes the.
     *
     * @param remove
     *            the new removes the
     */
    public void setRemove(boolean remove) {
        this._remove = remove;
    }

    /**
     * Checks if is clear.
     *
     * @return true, if is clear
     */
    public boolean isClear() {
        return _clear;
    }

    /**
     * Sets the clear.
     *
     * @param clear
     *            the new clear
     */
    public void setClear(boolean clear) {
        this._clear = clear;
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
