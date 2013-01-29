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

import org.overlord.sramp.common.SrampModelUtils;
import org.overlord.sramp.shell.api.AbstractShellCommand;
import org.overlord.sramp.shell.api.InvalidCommandArgumentException;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.Property;

/**
 * Command used to manipulate properties on the currently active S-RAMP artifact.  This command
 * can set and unset properties, both core properties and custom user properties.
 *
 * @author eric.wittmann@redhat.com
 */
public class PropertyCommand extends AbstractShellCommand {

	private static final Set<String> CORE_PROPERTIES = new HashSet<String>();
	{
		CORE_PROPERTIES.add("name");
		CORE_PROPERTIES.add("description");
		CORE_PROPERTIES.add("version");
	}

	/**
	 * Constructor.
	 */
	public PropertyCommand() {
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.ShellCommand#printUsage()
	 */
	@Override
	public void printUsage() {
		print("s-ramp:property <subCommand> <subCommandArguments>");
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.ShellCommand#printHelp()
	 */
	@Override
	public void printHelp() {
		print("The 'property' command manipulates the properties of the currently");
		print("active S-RAMP artifact.  The artifact must first be in the current");
		print("session through the s-ramp:getMetaData command.  This command sets");
		print("or unsets properties on that active artifact.  Both core meta-data");
		print("properties and custom (user-defined) properties can be manipulated");
		print("using this command.");
		print("");
		print("Supported sub-commands:  set, unset");
		print("");
		print("Example usage:");
		print(">  s-ramp:property set name MyArtifactName");
		print(">  s-ramp:property set customProp customVal");
		print(">  s-ramp:property unset customProp");
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.ShellCommand#execute()
	 */
	@Override
	public void execute() throws Exception {
		String subcmdArg = requiredArgument(0, "Please specify a sub-command (set, unset).");
		String propNameArg = requiredArgument(1, "Please specify a property name.");
		String propValueArg = null;
		if ("set".equals(subcmdArg)) {
			propValueArg = requiredArgument(2, "Please specify a property value.");
		}

		QName artifactVarName = new QName("s-ramp", "artifact");
		BaseArtifactType artifact = (BaseArtifactType) getContext().getVariable(artifactVarName);
		if (artifact == null) {
			print("No active S-RAMP artifact exists.  Use s-ramp:getMetaData.");
			return;
		}

		try {
			if ("set".equals(subcmdArg)) {
				setProperty(artifact, propNameArg, propValueArg);
				print("Successfully set property %1$s.", propNameArg);
			} else if ("unset".equals(subcmdArg)) {
				unsetProperty(artifact, propNameArg);
				print("Successfully unset property %1$s.", propNameArg);
			} else {
				throw new InvalidCommandArgumentException(0, "Invalid sub-command, must be either 'set' or 'unset'.");
			}
		} catch (InvalidCommandArgumentException e) {
			throw e;
		} catch (Exception e) {
			print("FAILED to change the artifact property.");
			print("\t" + e.getMessage());
		}
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
			if (propNameLC.equals("name")) {
				artifact.setName(propValue);
			} else if (propNameLC.equals("description")) {
				artifact.setDescription(propValue);
			} else if (propNameLC.equals("version")) {
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
		setProperty(artifact, propName, null);
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.AbstractShellCommand#tabCompletion(java.lang.String, java.util.List)
	 */
	@Override
	public int tabCompletion(String lastArgument, List<CharSequence> candidates) {
		QName artifactVarName = new QName("s-ramp", "artifact");

		if (getArguments().isEmpty()) {
			if (lastArgument == null) {
				candidates.add("set ");
				candidates.add("unset ");
				return 0;
			} else if ("set".startsWith(lastArgument)) {
				candidates.add("set ");
				return 0;
			} else if ("unset".startsWith(lastArgument)) {
				candidates.add("unset ");
				return 0;
			}
		} else if (getArguments().size() == 1 && (getArguments().contains("set") || getArguments().contains("unset"))) {
			BaseArtifactType artifact = (BaseArtifactType) getContext().getVariable(artifactVarName);
			if (artifact != null) {
				Set<String> props = new TreeSet<String>();
				props.addAll(CORE_PROPERTIES);
				props.addAll(getCustomPropertyNames(artifact));
				String candidatePostfix = " ";
				if (getArguments().contains("unset")) {
					candidatePostfix = "";
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
