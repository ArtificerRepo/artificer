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

import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.cl.Option;
import org.jboss.aesh.cl.completer.OptionCompleter;
import org.jboss.aesh.cl.validator.CommandValidator;
import org.jboss.aesh.cl.validator.CommandValidatorException;
import org.jboss.aesh.console.command.completer.CompleterInvocation;
import org.overlord.sramp.client.ontology.OntologySummary;
import org.overlord.sramp.shell.ShellCommandConstants;
import org.overlord.sramp.shell.i18n.Messages;


/**
 * Deletes an ontology.
 *
 * @author eric.wittmann@redhat.com
 */
@CommandDefinition(name = ShellCommandConstants.Ontology.ONTOLOGY_COMMAND_DELETE, description = "Deletes an ontology.", validator = DeleteOntologyCommand.CustomValidator.class)
public class DeleteOntologyCommand extends AbstractOntologyCommand {


    @Option(hasValue = true, name = "feedIndex", shortName = 'i', completer = OntologyCompleter.class)
    private Integer _feedIndex;

    @Option(hasValue = true, name = "uuid", shortName = 'u')
    private String _uuid;

    @Option(overrideRequired = true, name = "help", hasValue = false, shortName = 'h')
    private boolean _help;

    /**
     * Constructor.
     */
	public DeleteOntologyCommand() {
	}

	/**
     * Execute.
     *
     * @return true, if successful
     * @throws Exception
     *             the exception
     * @see org.overlord.sramp.shell.api.shell.ShellCommand#execute()
     */
	@SuppressWarnings("unchecked")
	@Override
	public boolean execute() throws Exception {
        super.execute();

		if (client == null) {
            print(Messages.i18n.format("MissingSRAMPConnection")); //$NON-NLS-1$
			return false;
		}
		String ontologyUuid = null;
        if (_feedIndex != null) { //$NON-NLS-1$
            OntologySummary ontology = getOntology(_feedIndex);
            ontologyUuid = ontology.getUuid();
        } else { //$NON-NLS-1$
            ontologyUuid = _uuid;
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



    /* (non-Javadoc)
     * @see org.overlord.sramp.shell.BuiltInShellCommand#getName()
     */
    @Override
    public String getName() {
        return ShellCommandConstants.Ontology.ONTOLOGY_COMMAND_DELETE;
    }

    /**
     * Validates that the Delete Ontology command is properly filled.
     *
     * @author David Virgil Naranjo
     */
    public class CustomValidator implements CommandValidator<DeleteOntologyCommand> {

        /**
         * Instantiates a new custom validator.
         */
        public CustomValidator() {

        }

        /* (non-Javadoc)
         * @see org.jboss.aesh.cl.validator.CommandValidator#validate(org.jboss.aesh.console.command.Command)
         */
        @Override
        public void validate(DeleteOntologyCommand command) throws CommandValidatorException {
            command.validateFeedOntology();
        }

    }

    /**
     * Completes the input string with the list of ontologies stored in s-ramp.
     *
     * @author David Virgil Naranjo
     */
    public class OntologyCompleter implements OptionCompleter<CompleterInvocation> {

        /**
         * Instantiates a new ontology completer.
         */
        public OntologyCompleter() {

        }

        /* (non-Javadoc)
         * @see org.jboss.aesh.cl.completer.OptionCompleter#complete(org.jboss.aesh.console.command.completer.CompleterInvocation)
         */
        @Override
        public void complete(CompleterInvocation completerInvocation) {
            completeOntology(completerInvocation);
        }

    }




    /* (non-Javadoc)
     * @see org.overlord.sramp.shell.commands.ontology.AbstractOntologyCommand#getFeedIndex()
     */
    @Override
    public Integer getFeedIndex() {
        return _feedIndex;
    }

    /**
     * Sets the feed index.
     *
     * @param feedIndex
     *            the new feed index
     */
    public void setFeedIndex(Integer feedIndex) {
        this._feedIndex = feedIndex;
    }

    /* (non-Javadoc)
     * @see org.overlord.sramp.shell.commands.ontology.AbstractOntologyCommand#getUuid()
     */
    @Override
    public String getUuid() {
        return _uuid;
    }

    /**
     * Sets the uuid.
     *
     * @param uuid
     *            the new uuid
     */
    public void setUuid(String uuid) {
        this._uuid = uuid;
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
