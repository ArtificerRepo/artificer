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
package org.overlord.sramp.shell.commands.ontology;

import java.util.List;

import javax.xml.namespace.QName;

import org.overlord.sramp.client.SrampAtomApiClient;
import org.overlord.sramp.client.ontology.OntologySummary;
import org.overlord.sramp.shell.BuiltInShellCommand;
import org.overlord.sramp.shell.api.InvalidCommandArgumentException;
import org.overlord.sramp.shell.i18n.Messages;

/**
 * Deletes an ontology.
 *
 * @author eric.wittmann@redhat.com
 */
public class DeleteOntologyCommand extends BuiltInShellCommand {

	/**
	 * Constructor.
	 */
	public DeleteOntologyCommand() {
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.ShellCommand#execute()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean execute() throws Exception {
		String ontologyIdArg = this.requiredArgument(0, Messages.i18n.format("DeleteOntology.InvalidArgMsg.OntologyId")); //$NON-NLS-1$

		QName feedVarName = new QName("ontology", "feed"); //$NON-NLS-1$ //$NON-NLS-2$
		QName clientVarName = new QName("s-ramp", "client"); //$NON-NLS-1$ //$NON-NLS-2$
		SrampAtomApiClient client = (SrampAtomApiClient) getContext().getVariable(clientVarName);
		if (client == null) {
            print(Messages.i18n.format("MissingSRAMPConnection")); //$NON-NLS-1$
			return false;
		}

		if (!ontologyIdArg.contains(":") || ontologyIdArg.endsWith(":")) { //$NON-NLS-1$ //$NON-NLS-2$
            throw new InvalidCommandArgumentException(0, Messages.i18n.format("InvalidOntologyIdFormat")); //$NON-NLS-1$
		}
		String ontologyUuid = null;
		int colonIdx = ontologyIdArg.indexOf(':');
		String idType = ontologyIdArg.substring(0, colonIdx);
		String idValue = ontologyIdArg.substring(colonIdx + 1);
		if ("feed".equals(idType)) { //$NON-NLS-1$
			List<OntologySummary> ontologies = (List<OntologySummary>) getContext().getVariable(feedVarName);
			if (ontologies == null) {
				throw new InvalidCommandArgumentException(0, Messages.i18n.format("DeleteOntology.NoOntologyFeed")); //$NON-NLS-1$
			}
			int feedIdx = Integer.parseInt(idValue) - 1;
			if (feedIdx < 0 || feedIdx >= ontologies.size()) {
                throw new InvalidCommandArgumentException(0, Messages.i18n.format("FeedIndexOutOfRange")); //$NON-NLS-1$
			}
			OntologySummary summary = ontologies.get(feedIdx);
			ontologyUuid = summary.getUuid();
		} else if ("uuid".equals(idType)) { //$NON-NLS-1$
			ontologyUuid = idValue;
		} else {
            throw new InvalidCommandArgumentException(0, Messages.i18n.format("InvalidIdFormat")); //$NON-NLS-1$
		}

		try {
			client.deleteOntology(ontologyUuid);
			print(Messages.i18n.format("DeleteOntology.Deleted")); //$NON-NLS-1$
		} catch (Exception e) {
			print(Messages.i18n.format("DeleteOntology.DeleteFailed")); //$NON-NLS-1$
			print("\t" + e.getMessage()); //$NON-NLS-1$
            return false;
		}
		return true;
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.AbstractShellCommand#tabCompletion(java.lang.String, java.util.List)
	 */
	@Override
	public int tabCompletion(String lastArgument, List<CharSequence> candidates) {
        return FeedTabCompleter.tabCompletion(getArguments(), getContext(), lastArgument, candidates);
	}
}
