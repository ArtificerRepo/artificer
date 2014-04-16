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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.cl.Option;
import org.jboss.aesh.cl.validator.CommandValidator;
import org.jboss.aesh.cl.validator.CommandValidatorException;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Property;
import org.overlord.sramp.common.SrampModelUtils;
import org.overlord.sramp.shell.ShellCommandConstants;
import org.overlord.sramp.shell.i18n.Messages;


/**
 * Command used to manipulate properties on the currently active S-RAMP artifact.  This command
 * can set and unset properties, both core properties and custom user properties.
 *
 * @author eric.wittmann@redhat.com
 */
@CommandDefinition(name = ShellCommandConstants.Sramp.S_RAMP_COMMAND_PROPERTY, validator = PropertyCommand.CustomCommandValidator.class, description = "Command used to manipulate properties on the currently active S-RAMP artifact.")
public class PropertyCommand extends AbstractCoreShellCommand {

    private static final Set<String> CORE_PROPERTIES = new HashSet<String>();
    {
        CORE_PROPERTIES.add("name"); //$NON-NLS-1$
        CORE_PROPERTIES.add("description"); //$NON-NLS-1$
        CORE_PROPERTIES.add("version"); //$NON-NLS-1$
    }

    @Option(hasValue = false, name = "set", shortName = 's')
    private boolean _set;

    @Option(hasValue = false, name = "unset", shortName = 'u')
    private boolean _unset;

    @Option(required = true, name = "name", shortName = 'n')
	private String _name;

    @Option(name = "value", shortName = 'v')
	private String _value;

    @Option(overrideRequired = true, name = "help", hasValue = false, shortName = 'h')
    private boolean _help;
	/**
	 * Constructor.
	 */
	public PropertyCommand() {
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
            if (_set) { //$NON-NLS-1$
                setProperty(artifact, _name, _value);
                print(Messages.i18n.format("Property.PropertySet", _name)); //$NON-NLS-1$
            } else if (_unset) { //$NON-NLS-1$
                unsetProperty(artifact, _name);
                print(Messages.i18n.format("Property.PropertyUnset", _name)); //$NON-NLS-1$
			}
		} catch (Exception e) {
			print(Messages.i18n.format("Property.Failure")); //$NON-NLS-1$
			print("\t" + e.getMessage()); //$NON-NLS-1$
            return false;
		}
        return true;
	}

	/**
     * Sets a property on the artifact.
     *
     * @param artifact
     *            the artifact
     * @param propName
     *            the prop name
     * @param propValue
     *            the prop value
     */
	private void setProperty(BaseArtifactType artifact, String propName, String propValue) {
		String propNameLC = propName.toLowerCase();
		if (CORE_PROPERTIES.contains(propNameLC)) {
			if (propNameLC.equals("name")) { //$NON-NLS-1$
				artifact.setName(propValue);
			} else if (propNameLC.equals("description")) { //$NON-NLS-1$
				artifact.setDescription(propValue);
			} else if (propNameLC.equals("version")) { //$NON-NLS-1$
				artifact.setVersion(propValue);
			}
		} else {
			SrampModelUtils.setCustomProperty(artifact, propName, propValue);
		}
	}

	/**
     * Unsets a property on the artifact.
     *
     * @param artifact
     *            the artifact
     * @param propName
     *            the prop name
     */
	private void unsetProperty(BaseArtifactType artifact, String propName) {
		setProperty(artifact, propName, null);
	}


	/**
     * Gets all the custom property names for the artifact.
     *
     * @param artifact
     *            the artifact
     * @return the custom property names
     */
	private Collection<String> getCustomPropertyNames(BaseArtifactType artifact) {
		Set<String> props = new HashSet<String>();
		for (Property prop : artifact.getProperty()) {
			props.add(prop.getPropertyName());
		}
		return props;
	}

    /* (non-Javadoc)
     * @see org.overlord.sramp.shell.BuiltInShellCommand#getName()
     */
    @Override
    public String getName() {
        return ShellCommandConstants.Sramp.S_RAMP_COMMAND_PROPERTY;
    }



    /**
     * Validates that the Property command is properly filled.
     * 
     * @author David Virgil Naranjo
     */
    public class CustomCommandValidator implements CommandValidator<PropertyCommand> {

        /**
         * Instantiates a new custom command validator.
         */
        public CustomCommandValidator() {

        }

        /* (non-Javadoc)
         * @see org.jboss.aesh.cl.validator.CommandValidator#validate(org.jboss.aesh.console.command.Command)
         */
        @Override
        public void validate(PropertyCommand command) throws CommandValidatorException {
            /*
             * try { command.validate(); } catch
             * (InvalidCommandArgumentException e) { throw new
             * CommandValidatorException(e.getMessage()); }
             */
            if (command._set && command._unset) {
                throw new CommandValidatorException(Messages.i18n.format("Property.both.set.unset.true"));
            } else if (!command._set && !command._unset) {
                throw new CommandValidatorException(Messages.i18n.format("Property.no.set.unset.true"));
            }
            if (command._set) {
                if (StringUtils.isBlank(command._value)) {
                    throw new CommandValidatorException(
                            Messages.i18n.format("Property.InvalidArgMsg.PropertyValue"));
                }

            }
        }

    }

    /**
     * Checks if is sets the.
     *
     * @return true, if is sets the
     */
    public boolean isSet() {
        return _set;
    }

    /**
     * Sets the sets the.
     *
     * @param set
     *            the new sets the
     */
    public void setSet(boolean set) {
        this._set = set;
    }

    /**
     * Checks if is unset.
     *
     * @return true, if is unset
     */
    public boolean isUnset() {
        return _unset;
    }

    /**
     * Sets the unset.
     *
     * @param unset
     *            the new unset
     */
    public void setUnset(boolean unset) {
        this._unset = unset;
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    public String getValue() {
        return _value;
    }

    /**
     * Sets the value.
     *
     * @param value
     *            the new value
     */
    public void setValue(String value) {
        this._value = value;
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

    /**
     * Sets the name.
     *
     * @param name
     *            the new name
     */
    public void setName(String name) {
        this._name = name;
    }

}
