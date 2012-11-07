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
package org.overlord.sramp.client.shell.commands.core;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.namespace.QName;

import org.overlord.sramp.client.shell.AbstractShellCommand;
import org.overlord.sramp.client.shell.commands.InvalidCommandArgumentException;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;

/**
 * Command used to manipulate classifications on the currently active S-RAMP artifact.  This command
 * can add and remove classifications.
 *
 * @author eric.wittmann@redhat.com
 */
public class ClassificationCommand extends AbstractShellCommand {

	private static final Set<String> subcommands = new HashSet<String>();
	{
		subcommands.add("add");
		subcommands.add("remove");
		subcommands.add("clear");
	}

	/**
	 * Constructor.
	 */
	public ClassificationCommand() {
	}

	/**
	 * @see org.overlord.sramp.client.shell.ShellCommand#printUsage()
	 */
	@Override
	public void printUsage() {
		print("s-ramp:classification <subCommand> <subCommandArguments>");
	}

	/**
	 * @see org.overlord.sramp.client.shell.ShellCommand#printHelp()
	 */
	@Override
	public void printHelp() {
		print("The 'classification' command manipulates the classifications of the");
		print("currently active S-RAMP artifact.  The artifact must first be in the");
		print("current session through the s-ramp:getMetaData command.  This command");
		print("adds or removes classifications on that active artifact.");
		print("");
		print("Example usage:");
		print(">  s-ramp:classification add http://www.example.org/regions.owl#China");
		print(">  s-ramp:classification remove http://www.example.org/regions.owl#China");
		print(">  s-ramp:classification clear");
	}

	/**
	 * @see org.overlord.sramp.client.shell.ShellCommand#execute()
	 */
	@Override
	public void execute() throws Exception {
		String subcmdArg = requiredArgument(0, "Please specify a sub-command (add, remove, clear).");
		String classificationArg = null;
		if ("add".equals(subcmdArg) || "remove".equals(subcmdArg)) {
			classificationArg = requiredArgument(1, "Please specify a classification URI.");
		}

		QName artifactVarName = new QName("s-ramp", "artifact");
		BaseArtifactType artifact = (BaseArtifactType) getContext().getVariable(artifactVarName);
		if (artifact == null) {
			print("No active S-RAMP artifact exists.  Use s-ramp:getMetaData.");
			return;
		}

		try {
			if ("add".equals(subcmdArg)) {
				artifact.getClassifiedBy().add(classificationArg);
				print("Successfully added classification '%1$s'.", classificationArg);
			} else if ("remove".equals(subcmdArg)) {
				if (artifact.getClassifiedBy().remove(classificationArg)) {
					print("Successfully removed classification '%1$s'.", classificationArg);
				} else {
					print("Classification '%1$s' does not exist on the active artifact.", classificationArg);
				}
			} else if ("clear".equals(subcmdArg)) {
				if (!artifact.getClassifiedBy().isEmpty()) {
					artifact.getClassifiedBy().clear();
					print("Successfully removed all classifications.");
				} else {
					print("No classifications exist on the active artifact.");
				}
			} else {
				throw new InvalidCommandArgumentException(0, "Invalid sub-command, must be one of ['add', 'remove', 'clear'].");
			}
		} catch (InvalidCommandArgumentException e) {
			throw e;
		} catch (Exception e) {
			print("FAILED to modify the artifact's classifications.");
			print("\t" + e.getMessage());
		}
	}

	/**
	 * @see org.overlord.sramp.client.shell.AbstractShellCommand#tabCompletion(java.lang.String, java.util.List)
	 */
	@Override
	public int tabCompletion(String lastArgument, List<CharSequence> candidates) {
		QName artifactVarName = new QName("s-ramp", "artifact");

		if (getArguments().isEmpty()) {
			for (String subcmd : subcommands) {
				if (lastArgument == null || subcmd.startsWith(lastArgument)) {
					candidates.add(subcmd + " ");
				}
			}
			return 0;
		} else if (getArguments().size() == 1 && getArguments().contains("remove")) {
			BaseArtifactType artifact = (BaseArtifactType) getContext().getVariable(artifactVarName);
			if (artifact != null) {
				Set<String> classifications = new TreeSet<String>();
				classifications.addAll(artifact.getClassifiedBy());
				for (String classification : classifications) {
					if (lastArgument == null || classification.startsWith(lastArgument)) {
						candidates.add(classification);
					}
				}
				return 0;
			}
		}
		return -1;
	}

}
