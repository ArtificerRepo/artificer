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
package org.artificer.shell.commands.core;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.namespace.QName;

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.artificer.shell.BuiltInShellCommand;
import org.artificer.shell.api.InvalidCommandArgumentException;
import org.artificer.shell.i18n.Messages;

/**
 * Command used to manipulate classifications on the currently active S-RAMP artifact.  This command
 * can add and remove classifications.
 *
 * @author eric.wittmann@redhat.com
 */
public class ClassificationCommand extends BuiltInShellCommand {

	private static final Set<String> subcommands = new HashSet<String>();
	{
		subcommands.add("add"); //$NON-NLS-1$
		subcommands.add("remove"); //$NON-NLS-1$
		subcommands.add("clear"); //$NON-NLS-1$
	}

	/**
	 * Constructor.
	 */
	public ClassificationCommand() {
	}

	@Override
	public boolean execute() throws Exception {
		String subcmdArg = requiredArgument(0, Messages.i18n.format("Classification.InvalidArgMsg")); //$NON-NLS-1$
		String classificationArg = null;
		if ("add".equals(subcmdArg) || "remove".equals(subcmdArg)) { //$NON-NLS-1$ //$NON-NLS-2$
			classificationArg = requiredArgument(1, Messages.i18n.format("Classification.InvalidArgMsg.ClassificationUri")); //$NON-NLS-1$
		}

		QName artifactVarName = new QName("s-ramp", "artifact"); //$NON-NLS-1$ //$NON-NLS-2$
		BaseArtifactType artifact = (BaseArtifactType) getContext().getVariable(artifactVarName);
		if (artifact == null) {
			print(Messages.i18n.format("NoActiveArtifact")); //$NON-NLS-1$
			return false;
		}

		try {
			if ("add".equals(subcmdArg)) { //$NON-NLS-1$
				artifact.getClassifiedBy().add(classificationArg);
				print(Messages.i18n.format("Classification.ClassificationAdded", classificationArg)); //$NON-NLS-1$
			} else if ("remove".equals(subcmdArg)) { //$NON-NLS-1$
				if (artifact.getClassifiedBy().remove(classificationArg)) {
					print(Messages.i18n.format("Classification.ClassificationRemoved"), classificationArg); //$NON-NLS-1$
				} else {
					print(Messages.i18n.format("Classification.ClassificationDoesNotExist"), classificationArg); //$NON-NLS-1$
					return false;
				}
			} else if ("clear".equals(subcmdArg)) { //$NON-NLS-1$
				if (!artifact.getClassifiedBy().isEmpty()) {
					artifact.getClassifiedBy().clear();
					print(Messages.i18n.format("Classification.AllRemoved")); //$NON-NLS-1$
				} else {
					print(Messages.i18n.format("Classification.NoneExist")); //$NON-NLS-1$
					return false;
				}
			} else {
				throw new InvalidCommandArgumentException(0, Messages.i18n.format("Classification.InvalidSubCommand")); //$NON-NLS-1$
			}
		} catch (InvalidCommandArgumentException e) {
			throw e;
		} catch (Exception e) {
			print(Messages.i18n.format("Classification.ModificationFailed")); //$NON-NLS-1$
			print("\t" + e.getMessage()); //$NON-NLS-1$
			return false;
		}

		return true;
	}

	@Override
	public int tabCompletion(String lastArgument, List<CharSequence> candidates) {
		QName artifactVarName = new QName("s-ramp", "artifact"); //$NON-NLS-1$ //$NON-NLS-2$

		if (getArguments().isEmpty()) {
			for (String subcmd : subcommands) {
				if (lastArgument == null || subcmd.startsWith(lastArgument)) {
					candidates.add(subcmd + " "); //$NON-NLS-1$
				}
			}
			return 0;
		} else if (getArguments().size() == 1 && getArguments().contains("remove")) { //$NON-NLS-1$
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
