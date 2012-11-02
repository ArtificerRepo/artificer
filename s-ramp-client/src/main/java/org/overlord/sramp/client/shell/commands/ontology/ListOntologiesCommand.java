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
import org.overlord.sramp.client.shell.ShellContext;

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
		System.out.println("ontology:list");
	}

	/**
	 * @see org.overlord.sramp.client.shell.ShellCommand#printHelp()
	 */
	@Override
	public void printHelp() {
		System.out.println("The 'list' command displays a list of all the ontologies.");
		System.out.println("currently known by the S-RAMP repository.  This list may be");
		System.out.println("empty if no ontologies have yet been added to the repository.");
		System.out.println("");
		System.out.println("Example usage:");
		System.out.println(">  ontology:list");
	}

	/**
	 * @see org.overlord.sramp.client.shell.ShellCommand#execute(org.overlord.sramp.client.shell.ShellContext)
	 */
	@Override
	public void execute(ShellContext context) throws Exception {
		QName clientVarName = new QName("s-ramp", "client");
		SrampAtomApiClient client = (SrampAtomApiClient) context.getVariable(clientVarName);
		if (client == null) {
			System.out.println("No S-RAMP repository connection is currently open.");
			return;
		}
		try {
			List<OntologySummary> ontologies = client.getOntologies();
			System.out.printf("Ontologies (%1$d entries)\n", ontologies.size());
			System.out.printf("  Idx  Base\n");
			System.out.printf("  ---  ----\n");
			int idx = 0;
			for (OntologySummary ontology : ontologies) {
				String base = ontology.getBase();
				System.out.printf("  %1$3d  %2$s\n", idx++, base);
			}
		} catch (Exception e) {
			System.out.println("FAILED to get the list of ontologies.");
			System.out.println("\t" + e.getMessage());
		}
	}
}
