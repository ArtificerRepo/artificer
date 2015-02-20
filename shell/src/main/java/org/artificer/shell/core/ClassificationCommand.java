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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Command used to manipulate classifications on the currently active S-RAMP artifact.  This command
 * can add and remove classifications.
 *
 * @author Brett Meyer
 * @author eric.wittmann@redhat.com
 */
@CommandDefinition(name = "classification",
		description = "The \"classification\" command manipulates the classifications of the currently active Artificer artifact.  The artifact must first be in the current session through the getMetaData command.  This command adds or removes classifications on that active artifact.\nSupported sub-commands:  add, remove, clear\nExample usage:\nclassification add http://www.example.org/regions.owl#China\nclassification remove http://www.example.org/regions.owl#China\nclassification clear\n")
public class ClassificationCommand extends AbstractCommand {

	private static final Set<String> subcommands = new HashSet<String>();
	{
		subcommands.add("add");
		subcommands.add("remove");
		subcommands.add("clear");
	}

	@Arguments(description = "add|remove|clear [<classification>]", completer = Completer.class)
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
		String classificationArg = null;
		if ("add".equals(subcmdArg) || "remove".equals(subcmdArg)) {
			classificationArg = requiredArgument(commandInvocation, arguments, 1);
		}

		BaseArtifactType artifact = currentArtifact(commandInvocation);

		try {
			if ("add".equals(subcmdArg)) {
				artifact.getClassifiedBy().add(classificationArg);
				commandInvocation.getShell().out().println(Messages.i18n.format("Classification.ClassificationAdded", classificationArg));
			} else if ("remove".equals(subcmdArg)) {
				if (artifact.getClassifiedBy().remove(classificationArg)) {
					commandInvocation.getShell().out().println(Messages.i18n.format("Classification.ClassificationRemoved", classificationArg));
				} else {
					commandInvocation.getShell().out().println(Messages.i18n.format("Classification.ClassificationDoesNotExist", classificationArg));
					return CommandResult.FAILURE;
				}
			} else if ("clear".equals(subcmdArg)) {
				if (!artifact.getClassifiedBy().isEmpty()) {
					artifact.getClassifiedBy().clear();
					commandInvocation.getShell().out().println(Messages.i18n.format("Classification.AllRemoved"));
				} else {
					commandInvocation.getShell().out().println(Messages.i18n.format("Classification.NoneExist"));
					return CommandResult.FAILURE;
				}
			} else {
				commandInvocation.getShell().out().println(Messages.i18n.format("Classification.InvalidSubCommand"));
				return CommandResult.FAILURE;
			}
		} catch (Exception e) {
			commandInvocation.getShell().out().println(Messages.i18n.format("Classification.ModificationFailed"));
			commandInvocation.getShell().out().println("\t" + e.getMessage());
			return CommandResult.FAILURE;
		}

		return CommandResult.SUCCESS;
	}

	private static class Completer implements OptionCompleter<CompleterInvocation> {
		@Override
		public void complete(CompleterInvocation completerInvocation) {
			ClassificationCommand command = (ClassificationCommand) completerInvocation.getCommand();
			String currentValue = completerInvocation.getGivenCompleteValue();
			if (CollectionUtils.isEmpty(command.arguments)) {
				for (String subcmd : subcommands) {
					if (StringUtils.isBlank(currentValue) || subcmd.startsWith(currentValue)) {
						completerInvocation.addCompleterValue(subcmd);
					}
				}
			} else if (command.arguments.size() == 1 && command.arguments.contains("remove")) {
				ArtificerContext artificerContext = (ArtificerContext) completerInvocation.getAeshContext();
				BaseArtifactType artifact = artificerContext.getCurrentArtifact();
				if (artifact != null) {
					Set<String> classifications = new TreeSet<String>();
					classifications.addAll(artifact.getClassifiedBy());
					for (String classification : classifications) {
						if (StringUtils.isBlank(currentValue) || classification.startsWith(currentValue)) {
							completerInvocation.addCompleterValue(classification);
						}
					}
				}
			}
		}
	}

	@Override
	protected String getName() {
		return "classification";
	}

}
