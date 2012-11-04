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
package org.overlord.sramp.client.shell.commands.ontology;

import java.util.List;

import javax.xml.namespace.QName;

import org.overlord.sramp.client.SrampAtomApiClient;
import org.overlord.sramp.client.ontology.OntologySummary;
import org.overlord.sramp.client.shell.AbstractShellCommand;

/**
 * Lists all ontologies in the S-RAMP repository.
 *
 * @author eric.wittmann@redhat.com
 */
public class ListOntologiesCommand extends AbstractShellCommand {

	/**
	 * Constructor.
	 */
	public ListOntologiesCommand() {
	}

	/**
	 * @see org.overlord.sramp.client.shell.ShellCommand#printUsage()
	 */
	@Override
	public void printUsage() {
		print("ontology:list");
	}

	/**
	 * @see org.overlord.sramp.client.shell.ShellCommand#printHelp()
	 */
	@Override
	public void printHelp() {
		print("The 'list' command displays a list of all the ontologies");
		print("currently known to the S-RAMP repository.  This list may be");
		print("empty if no ontologies have yet been added to the repository.");
		print("");
		print("Example usage:");
		print("> ontology:list");
	}

	/**
	 * @see org.overlord.sramp.client.shell.ShellCommand#execute()
	 */
	@Override
	public void execute() throws Exception {
		QName clientVarName = new QName("s-ramp", "client");
		QName feedVarName = new QName("ontology", "feed");
		SrampAtomApiClient client = (SrampAtomApiClient) getContext().getVariable(clientVarName);
		if (client == null) {
			print("No S-RAMP repository connection is currently open.");
			return;
		}
		try {
			List<OntologySummary> ontologies = client.getOntologies();
			print("Ontologies (%1$d entries)", ontologies.size());
			print("  Idx  Base");
			print("  ---  ----");
			int idx = 1;
			for (OntologySummary ontology : ontologies) {
				String base = ontology.getBase();
				print("  %1$3d  %2$s", idx++, base);
			}

			getContext().setVariable(feedVarName, ontologies);
		} catch (Exception e) {
			print("FAILED to get the list of ontologies.");
			print("\t" + e.getMessage());
		}
	}
}
