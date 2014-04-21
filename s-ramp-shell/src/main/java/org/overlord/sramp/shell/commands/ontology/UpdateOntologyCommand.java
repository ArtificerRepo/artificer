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

import java.io.File;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.cl.Option;
import org.jboss.aesh.cl.completer.FileOptionCompleter;
import org.jboss.aesh.cl.completer.OptionCompleter;
import org.jboss.aesh.cl.validator.CommandValidator;
import org.jboss.aesh.cl.validator.CommandValidatorException;
import org.jboss.aesh.console.command.completer.CompleterInvocation;
import org.overlord.sramp.client.ontology.OntologySummary;
import org.overlord.sramp.shell.ShellCommandConstants;
import org.overlord.sramp.shell.aesh.RequiredOptionRenderer;
import org.overlord.sramp.shell.i18n.Messages;


/**
 * Updates an ontology (S-RAMP OWL format).
 *
 * @author eric.wittmann@redhat.com
 */
@CommandDefinition(name = ShellCommandConstants.Ontology.ONTOLOGY_COMMAND_UPDATE, description = "Updates an ontology (S-RAMP OWL format).", validator = UpdateOntologyCommand.CustomValidator.class)
public class UpdateOntologyCommand extends AbstractOntologyCommand {

    @Option(hasValue = true, name = "file", required = true, shortName = 'f', completer = FileOptionCompleter.class, renderer = RequiredOptionRenderer.class)
    private File _file;

    @Option(hasValue = true, name = "feedIndex", shortName = 'i', completer = OntologyCompleter.class)
    private Integer _feedIndex;

    @Option(hasValue = true, name = "uuid", shortName = 'u')
    private String _uuid;

    @Option(overrideRequired = true, name = "help", hasValue = false, shortName = 'h')
    private boolean _help;


    /**
     * Constructor.
     */
	public UpdateOntologyCommand() {
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
        String ontologyUuid = null;
        if (_feedIndex != null) { //$NON-NLS-1$
            OntologySummary ontology = getOntology(_feedIndex);
            ontologyUuid = ontology.getUuid();
        } else { //$NON-NLS-1$
            ontologyUuid = _uuid;
        }
        InputStream content = null;
		try {
		    if (_file.exists()) {
		        content = FileUtils.openInputStream(_file);
		    } else {
                URL url = this.getClass().getResource(_file.getAbsolutePath());
		        if (url != null) {
		            print(Messages.i18n.format("UpdateOntology.ReadingOntology", url.toExternalForm())); //$NON-NLS-1$
		            content = url.openStream();
		        } else {
                    print(Messages.i18n.format("UpdateOntology.CannotFind", _file.getAbsolutePath())); //$NON-NLS-1$
                    return false;
		        }
		    }
	        client.updateOntology(ontologyUuid, content);
			print(Messages.i18n.format("UpdateOntology.SuccessfulUpdate")); //$NON-NLS-1$
		} catch (Exception e) {
			print(Messages.i18n.format("UpdateOntology.UpdateFailed")); //$NON-NLS-1$
			print("\t" + e.getMessage()); //$NON-NLS-1$
			IOUtils.closeQuietly(content);
            return false;
		} finally {
			IOUtils.closeQuietly(content);
		}
        return true;
	}



    /* (non-Javadoc)
     * @see org.overlord.sramp.shell.BuiltInShellCommand#getName()
     */
    @Override
    public String getName() {
        return ShellCommandConstants.Ontology.ONTOLOGY_COMMAND_UPDATE;
    }

    /**
     * Validates that the Update Ontology command is properly filled.
     *
     * @author David Virgil Naranjo
     */
    public class CustomValidator implements CommandValidator<UpdateOntologyCommand> {

        /**
         * Instantiates a new custom validator.
         */
        public CustomValidator() {

        }

        /* (non-Javadoc)
         * @see org.jboss.aesh.cl.validator.CommandValidator#validate(org.jboss.aesh.console.command.Command)
         */
        @Override
        public void validate(UpdateOntologyCommand command) throws CommandValidatorException {
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

    /**
     * Gets the file.
     *
     * @return the file
     */
    public File getFile() {
        return _file;
    }

    /**
     * Sets the file.
     *
     * @param file
     *            the new file
     */
    public void setFile(File file) {
        this._file = file;
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
