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
import org.overlord.sramp.shell.api.AbstractShellCommand;
import org.overlord.sramp.shell.api.InvalidCommandArgumentException;

/**
 * Deletes an ontology.
 *
 * @author eric.wittmann@redhat.com
 */
public class DeleteOntologyCommand extends AbstractShellCommand {

	/**
	 * Constructor.
	 */
	public DeleteOntologyCommand() {
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.ShellCommand#printUsage()
	 */
	@Override
	public void printUsage() {
		print("ontology:delete <ontologyId>");
		print("\tValid formats for ontologyId:");
		print("\t  feed:<feedIndex> - an index into the most recent list of ontologies");
		print("\t  uuid:<ontologyUUID> - the UUID of an ontology");
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.ShellCommand#printHelp()
	 */
	@Override
	public void printHelp() {
		print("The 'delete' command removes a single ontology from the S-RAMP");
		print("repository.");
		print("");
		print("Example usage:");
		print("> ontology:delete feed:2");
		print("> ontology:delete uuid:2763-2382-39293-382873");
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.ShellCommand#execute()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void execute() throws Exception {
		String ontologyIdArg = this.requiredArgument(0, "Please specify a valid ontology identifier.");

		QName feedVarName = new QName("ontology", "feed");
		QName clientVarName = new QName("s-ramp", "client");
		SrampAtomApiClient client = (SrampAtomApiClient) getContext().getVariable(clientVarName);
		if (client == null) {
			print("No S-RAMP repository connection is currently open.");
			return;
		}

		if (!ontologyIdArg.contains(":") || ontologyIdArg.endsWith(":")) {
			throw new InvalidCommandArgumentException(0, "Invalid artifact id format.");
		}
		String ontologyUuid = null;
		int colonIdx = ontologyIdArg.indexOf(':');
		String idType = ontologyIdArg.substring(0, colonIdx);
		String idValue = ontologyIdArg.substring(colonIdx + 1);
		if ("feed".equals(idType)) {
			List<OntologySummary> ontologies = (List<OntologySummary>) getContext().getVariable(feedVarName);
			if (ontologies == null) {
				throw new InvalidCommandArgumentException(0, "There is no ontology feed available, try 'ontology:list' first.");
			}
			int feedIdx = Integer.parseInt(idValue) - 1;
			if (feedIdx < 0 || feedIdx >= ontologies.size()) {
				throw new InvalidCommandArgumentException(0, "Feed index out of range.");
			}
			OntologySummary summary = ontologies.get(feedIdx);
			ontologyUuid = summary.getUuid();
		} else if ("uuid".equals(idType)) {
			ontologyUuid = idValue;
		} else {
			throw new InvalidCommandArgumentException(0, "Invalid artifact id format.");
		}

		try {
			client.deleteOntology(ontologyUuid);
			print("Successfully deleted the ontology.");
		} catch (Exception e) {
			print("FAILED to get the list of ontologies.");
			print("\t" + e.getMessage());
		}
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.AbstractShellCommand#tabCompletion(java.lang.String, java.util.List)
	 */
	@Override
	public int tabCompletion(String lastArgument, List<CharSequence> candidates) {
		if (getArguments().isEmpty() && (lastArgument == null || "feed:".startsWith(lastArgument))) {
			QName feedVarName = new QName("ontology", "feed");
			@SuppressWarnings("unchecked")
			List<OntologySummary> ontologies = (List<OntologySummary>) getContext().getVariable(feedVarName);
			if (ontologies != null) {
				for (int idx = 0; idx < ontologies.size(); idx++) {
					String candidate = "feed:" + (idx+1);
					if (lastArgument == null) {
						candidates.add(candidate);
					}
					if (lastArgument != null && candidate.startsWith(lastArgument)) {
						candidates.add(candidate);
					}
				}
			}
			return 0;
		} else {
			return -1;
		}
	}
}
