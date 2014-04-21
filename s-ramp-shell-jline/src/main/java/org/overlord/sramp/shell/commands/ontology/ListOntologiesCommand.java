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
import org.overlord.sramp.shell.i18n.Messages;

/**
 * Lists all ontologies in the S-RAMP repository.
 *
 * @author eric.wittmann@redhat.com
 */
public class ListOntologiesCommand extends BuiltInShellCommand {

	/**
	 * Constructor.
	 */
	public ListOntologiesCommand() {
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.ShellCommand#execute()
	 */
	@Override
	public boolean execute() throws Exception {
		QName clientVarName = new QName("s-ramp", "client"); //$NON-NLS-1$ //$NON-NLS-2$
		QName feedVarName = new QName("ontology", "feed"); //$NON-NLS-1$ //$NON-NLS-2$
		SrampAtomApiClient client = (SrampAtomApiClient) getContext().getVariable(clientVarName);
		if (client == null) {
			print(Messages.i18n.format("MissingSRAMPConnection")); //$NON-NLS-1$
			return false;
		}
		try {
			List<OntologySummary> ontologies = client.getOntologies();
			print(Messages.i18n.format("ListOntologies.Summary", ontologies.size())); //$NON-NLS-1$
			print("  Idx  " + Messages.i18n.format("ListOntologies.Base")); //$NON-NLS-1$ //$NON-NLS-2$
			print("  ---  ----"); //$NON-NLS-1$
			int idx = 1;
			for (OntologySummary ontology : ontologies) {
				String base = ontology.getBase();
				print("  %1$3d  %2$s", idx++, base); //$NON-NLS-1$
			}

			getContext().setVariable(feedVarName, ontologies);
		} catch (Exception e) {
			print(Messages.i18n.format("ListOntologies.Failed")); //$NON-NLS-1$
			print("\t" + e.getMessage()); //$NON-NLS-1$
            return false;
		}
        return true;
	}
}
