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
package org.artificer.shell.core;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.artificer.common.ArtificerModelUtils;
import org.artificer.shell.AbstractCommand;
import org.artificer.shell.ArtificerContext;
import org.artificer.shell.i18n.Messages;
import org.jboss.aesh.cl.Arguments;
import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.cl.Option;
import org.jboss.aesh.cl.completer.OptionCompleter;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.command.completer.CompleterInvocation;
import org.jboss.aesh.console.command.invocation.CommandInvocation;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Property;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Command used to manipulate properties on the currently active S-RAMP artifact.  This command
 * can set and unset properties, both core properties and custom user properties.
 *
 * @author Brett Meyer
 * @author eric.wittmann@redhat.com
 */
@CommandDefinition(name = "property",
		description = "The \"property\" command manipulates the properties of the currently active Artificer artifact.  The artifact must first be in the current session through the getMetaData command.  This command sets or unsets properties on that active artifact.  Both core meta-data properties and custom (user-defined) properties can be manipulated using this command.\nSupported sub-commands: set, unset\nExample usage:\nproperty set name MyArtifactName\nproperty set customProp customVal\nproperty unset customProp\n")
public class PropertyCommand extends AbstractCommand {

	private static final Set<String> CORE_PROPERTIES = new HashSet<String>();
	{
		CORE_PROPERTIES.add("name");
		CORE_PROPERTIES.add("description");
		CORE_PROPERTIES.add("version");
	}

	@Arguments(description = "set <property name> <property value> | unset <property name>", completer = Completer.class)
	private List<String> arguments;

	@Option(shortName = 'h', name = "help", hasValue = false, required = false, overrideRequired = true,
			description = "Display help")
	private boolean help;

	@Override
	protected CommandResult doExecute(CommandInvocation commandInvocation) throws Exception {
		if (help) {
			return doHelp(commandInvocation);
		}
		if (CollectionUtils.isEmpty(arguments)) {
			return doHelp(commandInvocation);
		}

		String subcmdArg = requiredArgument(commandInvocation, arguments, 0);
		String propNameArg = requiredArgument(commandInvocation, arguments, 1);
		String propValueArg = null;
		if ("set".equals(subcmdArg)) {
			propValueArg = requiredArgument(commandInvocation, arguments, 2);
		}

		BaseArtifactType artifact = currentArtifact(commandInvocation);

		try {
			if ("set".equals(subcmdArg)) {
				setProperty(artifact, propNameArg, propValueArg);
				commandInvocation.getShell().out().println(Messages.i18n.format("Property.PropertySet", propNameArg));
			} else if ("unset".equals(subcmdArg)) {
				unsetProperty(artifact, propNameArg);
				commandInvocation.getShell().out().println(Messages.i18n.format("Property.PropertyUnset", propNameArg));
			} else {
				commandInvocation.getShell().out().println(Messages.i18n.format("Property.InvalidSubCommand"));
				return CommandResult.FAILURE;
			}
		} catch (Exception e) {
			commandInvocation.getShell().out().println(Messages.i18n.format("Property.Failure"));
			commandInvocation.getShell().out().println("\t" + e.getMessage());
            return CommandResult.FAILURE;
		}
        return CommandResult.SUCCESS;
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
			ArtificerModelUtils.setCustomProperty(artifact, propName, propValue);
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
			ArtificerModelUtils.unsetCustomProperty(artifact, propName);
		}
	}

	private static class Completer implements OptionCompleter<CompleterInvocation> {
		@Override
		public void complete(CompleterInvocation completerInvocation) {
			PropertyCommand command = (PropertyCommand) completerInvocation.getCommand();
			String currentValue = completerInvocation.getGivenCompleteValue();
			if (CollectionUtils.isEmpty(command.arguments)) {
				if (StringUtils.isBlank(currentValue)) {
					completerInvocation.addCompleterValue("set");
					completerInvocation.addCompleterValue("unset");
				} else if ("set".startsWith(currentValue)) {
					completerInvocation.addCompleterValue("set");
				} else if ("unset".startsWith(currentValue)) {
					completerInvocation.addCompleterValue("unset");
				}
			} else if (command.arguments.size() == 1) {
				ArtificerContext artificerContext = (ArtificerContext) completerInvocation.getAeshContext();
				BaseArtifactType artifact = artificerContext.getCurrentArtifact();
				if (artifact != null) {
					Set<String> props = new TreeSet<String>();
					props.addAll(CORE_PROPERTIES);
					props.addAll(getCustomPropertyNames(artifact));
					for (String prop : props) {
						if (StringUtils.isBlank(currentValue) || prop.startsWith(currentValue)) {
							completerInvocation.addCompleterValue(prop);
						}
					}
				}
			}
		}
	}

	/**
	 * Gets all the custom property names for the artifact.
	 * @param artifact
	 */
	private static Collection<String> getCustomPropertyNames(BaseArtifactType artifact) {
		Set<String> props = new HashSet<String>();
		for (Property prop : artifact.getProperty()) {
			props.add(prop.getPropertyName());
		}
		return props;
	}

	@Override
	protected String getName() {
		return "property";
	}

}
