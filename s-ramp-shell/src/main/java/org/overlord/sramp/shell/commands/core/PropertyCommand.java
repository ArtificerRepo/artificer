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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.namespace.QName;

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Property;
import org.overlord.sramp.common.SrampModelUtils;
import org.overlord.sramp.shell.BuiltInShellCommand;
import org.overlord.sramp.shell.api.InvalidCommandArgumentException;
import org.overlord.sramp.shell.i18n.Messages;

/**
 * Command used to manipulate properties on the currently active S-RAMP artifact.  This command
 * can set and unset properties, both core properties and custom user properties.
 *
 * @author eric.wittmann@redhat.com
 */
public class PropertyCommand extends BuiltInShellCommand {

	private static final Set<String> CORE_PROPERTIES = new HashSet<String>();
	{
		CORE_PROPERTIES.add("name"); //$NON-NLS-1$
		CORE_PROPERTIES.add("description"); //$NON-NLS-1$
		CORE_PROPERTIES.add("version"); //$NON-NLS-1$
	}

	/**
	 * Constructor.
	 */
	public PropertyCommand() {
	}

	@Override
	public boolean execute() throws Exception {
		String subcmdArg = requiredArgument(0, Messages.i18n.format("Property.InvalidArgMsg.SubCommand")); //$NON-NLS-1$
		String propNameArg = requiredArgument(1, Messages.i18n.format("Property.InvalidArgMsg.PropertyName")); //$NON-NLS-1$
		String propValueArg = null;
		if ("set".equals(subcmdArg)) { //$NON-NLS-1$
			propValueArg = optionalArgument(2);
		}

		QName artifactVarName = new QName("s-ramp", "artifact"); //$NON-NLS-1$ //$NON-NLS-2$
		BaseArtifactType artifact = (BaseArtifactType) getContext().getVariable(artifactVarName);
		if (artifact == null) {
			print(Messages.i18n.format("NoActiveArtifact")); //$NON-NLS-1$
			return false;
		}

		try {
			if ("set".equals(subcmdArg)) { //$NON-NLS-1$
				setProperty(artifact, propNameArg, propValueArg);
				print(Messages.i18n.format("Property.PropertySet", propNameArg)); //$NON-NLS-1$
			} else if ("unset".equals(subcmdArg)) { //$NON-NLS-1$
				unsetProperty(artifact, propNameArg);
				print(Messages.i18n.format("Property.PropertyUnset", propNameArg)); //$NON-NLS-1$
			} else {
				throw new InvalidCommandArgumentException(0, Messages.i18n.format("Property.InvalidSubCommand")); //$NON-NLS-1$
			}
		} catch (InvalidCommandArgumentException e) {
			throw e;
		} catch (Exception e) {
			print(Messages.i18n.format("Property.Failure")); //$NON-NLS-1$
			print("\t" + e.getMessage()); //$NON-NLS-1$
            return false;
		}
        return true;
	}

	/**
	 * Sets a property on the artifact.
	 * @param artifact
	 * @param propName
	 * @param propValue
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
	 * @param artifact
	 * @param propName
	 */
	private void unsetProperty(BaseArtifactType artifact, String propName) {
		String propNameLC = propName.toLowerCase();
		if (CORE_PROPERTIES.contains(propNameLC)) {
			setProperty(artifact, propName, null);
		} else {
			SrampModelUtils.unsetCustomProperty(artifact, propName);
		}
	}

	@Override
	public int tabCompletion(String lastArgument, List<CharSequence> candidates) {
		QName artifactVarName = new QName("s-ramp", "artifact"); //$NON-NLS-1$ //$NON-NLS-2$

		if (getArguments().isEmpty()) {
			if (lastArgument == null) {
				candidates.add("set "); //$NON-NLS-1$
				candidates.add("unset "); //$NON-NLS-1$
				return 0;
			} else if ("set".startsWith(lastArgument)) { //$NON-NLS-1$
				candidates.add("set "); //$NON-NLS-1$
				return 0;
			} else if ("unset".startsWith(lastArgument)) { //$NON-NLS-1$
				candidates.add("unset "); //$NON-NLS-1$
				return 0;
			}
		} else if (getArguments().size() == 1 && (getArguments().contains("set") || getArguments().contains("unset"))) { //$NON-NLS-1$ //$NON-NLS-2$
			BaseArtifactType artifact = (BaseArtifactType) getContext().getVariable(artifactVarName);
			if (artifact != null) {
				Set<String> props = new TreeSet<String>();
				props.addAll(CORE_PROPERTIES);
				props.addAll(getCustomPropertyNames(artifact));
				String candidatePostfix = " "; //$NON-NLS-1$
				if (getArguments().contains("unset")) { //$NON-NLS-1$
					candidatePostfix = ""; //$NON-NLS-1$
				}
				for (String prop : props) {
					if (lastArgument == null || prop.startsWith(lastArgument)) {
						candidates.add(prop + candidatePostfix);
					}
				}
				return 0;
			}
		}
		return -1;
	}

	/**
	 * Gets all the custom property names for the artifact.
	 * @param artifact
	 */
	private Collection<String> getCustomPropertyNames(BaseArtifactType artifact) {
		Set<String> props = new HashSet<String>();
		for (Property prop : artifact.getProperty()) {
			props.add(prop.getPropertyName());
		}
		return props;
	}

}
