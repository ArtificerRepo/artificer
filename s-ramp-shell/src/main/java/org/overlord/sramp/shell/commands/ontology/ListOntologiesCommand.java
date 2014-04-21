/*
 * Copyright 2014 JBoss Inc
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

import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.cl.Option;
import org.overlord.sramp.client.ontology.OntologySummary;
import org.overlord.sramp.shell.ShellCommandConstants;
import org.overlord.sramp.shell.i18n.Messages;


/**
 * Lists all ontologies in the S-RAMP repository.
 *
 * @author eric.wittmann@redhat.com
 */
@CommandDefinition(name = ShellCommandConstants.Ontology.ONTOLOGY_COMMAND_LIST, description = "Lists all ontologies in the S-RAMP repository.")
public class ListOntologiesCommand extends AbstractOntologyCommand {

    @Option(overrideRequired = true, name = "help", hasValue = false, shortName = 'h')
    private boolean _help;

    /**
     * Constructor.
     */
	public ListOntologiesCommand() {
	}

	/**
     * Execute.
     *
     * @return true, if successful
     * @throws Exception
     *             the exception
     * @see org.overlord.sramp.shell.api.shell.ShellCommand#execute()
     */
	@Override
	public boolean execute() throws Exception {
        super.execute();

		if (client == null) {
			print(Messages.i18n.format("MissingSRAMPConnection")); //$NON-NLS-1$
			return false;
		}
		try {
			List<OntologySummary> ontologies = client.getOntologies();
			print(Messages.i18n.format("ListOntologies.Summary", ontologies.size())); //$NON-NLS-1$
            print("  Idx                                  UUID  " + Messages.i18n.format("ListOntologies.Base")); //$NON-NLS-1$ //$NON-NLS-2$
            print("  ---                                  ----  ----"); //$NON-NLS-1$
			int idx = 1;
			for (OntologySummary ontology : ontologies) {
				String base = ontology.getBase();
                print("  %1$3d %2$37s %3$s", idx++, ontology.getUuid(), base); //$NON-NLS-1$
			}

			getContext().setVariable(feedVarName, ontologies);
		} catch (Exception e) {
			print(Messages.i18n.format("ListOntologies.Failed")); //$NON-NLS-1$
			print("\t" + e.getMessage()); //$NON-NLS-1$
            return false;
		}
        return true;
	}

    /* (non-Javadoc)
     * @see org.overlord.sramp.shell.BuiltInShellCommand#getName()
     */
    @Override
    public String getName() {
        return ShellCommandConstants.Ontology.ONTOLOGY_COMMAND_LIST;
    }

    /* (non-Javadoc)
     * @see org.overlord.sramp.shell.BuiltInShellCommand#isHelp()
     */
    @Override
    public boolean isHelp() {
        return _help;
    }

    /**
     * Sets the help.
     *
     * @param help
     *            the new help
     */
    public void setHelp(boolean help) {
        this._help = help;
    }

}
