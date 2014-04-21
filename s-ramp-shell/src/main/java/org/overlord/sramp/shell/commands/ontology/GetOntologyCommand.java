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
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

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
import org.w3._1999._02._22_rdf_syntax_ns_.RDF;


/**
 * Gets an ontology (and saves it to a local file).
 *
 * @author eric.wittmann@redhat.com
 */
@CommandDefinition(name = ShellCommandConstants.Ontology.ONTOLOGY_COMMAND_GET, description = "Gets an ontology (and saves it to a local file).", validator = GetOntologyCommand.CustomValidator.class)
public class GetOntologyCommand extends AbstractOntologyCommand {

    @Option(hasValue = true, required = true, name = "outputFile", shortName = 'f', completer = FileOptionCompleter.class, renderer = RequiredOptionRenderer.class)
    private File _outputFile;

    @Option(hasValue = true, name = "feedIndex", shortName = 'i', completer = OntologyCompleter.class)
    private Integer _feedIndex;

    @Option(hasValue = true, name = "uuid", shortName = 'u')
    private String _uuid;


    @Option(overrideRequired = true, name = "help", hasValue = false, shortName = 'h')
    private boolean _help;


    /**
     * Constructor.
     */
	public GetOntologyCommand() {
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
			RDF ontology = client.getOntology(ontologyUuid);

            if (_outputFile != null) {
                saveOntologyToFile(ontology, _outputFile);
			} else {
			    printOntology(ontology);
			}
		} catch (Exception e) {
			print(Messages.i18n.format("GetOntology.GetFailed")); //$NON-NLS-1$
			print("\t" + e.getMessage()); //$NON-NLS-1$
            return false;
		}
		return true;
	}

	/**
     * Outputs an RDF to stdout.
     *
     * @param ontology
     *            the ontology
     * @throws Exception
     *             the exception
     */
    private void printOntology(RDF ontology) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(RDF.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.marshal(ontology, System.out);
    }

    /**
     * Outputs an RDF to a file.
     *
     * @param ontology
     *            the ontology
     * @param filePath
     *            the file path
     * @throws Exception
     *             the exception
     */
    private void saveOntologyToFile(RDF ontology, File filePath) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(RDF.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.marshal(ontology, filePath);
        print(Messages.i18n.format("GetOntology.OntologySaved", filePath.getCanonicalPath())); //$NON-NLS-1$
    }


    /* (non-Javadoc)
     * @see org.overlord.sramp.shell.BuiltInShellCommand#getName()
     */
    @Override
    public String getName() {
        return ShellCommandConstants.Ontology.ONTOLOGY_COMMAND_GET;
    }

    /**
     * Validates that the Get Ontology command is properly filled.
     *
     * @author David Virgil Naranjo
     */
    public class CustomValidator implements CommandValidator<GetOntologyCommand> {

        /**
         * Instantiates a new custom validator.
         */
        public CustomValidator() {

        }

        /* (non-Javadoc)
         * @see org.jboss.aesh.cl.validator.CommandValidator#validate(org.jboss.aesh.console.command.Command)
         */
        @Override
        public void validate(GetOntologyCommand command) throws CommandValidatorException {
            command.validateFeedOntology();
            if (command._outputFile.exists()) {
                try {
                    throw new CommandValidatorException(Messages.i18n.format("GetOntology.PathExists",
                            command._outputFile.getCanonicalPath()));
                } catch (IOException e) {
                    throw new CommandValidatorException(Messages.i18n.format("GetOntology.PathExists"));
                }
            }
            if (command._outputFile.getParentFile() != null && command._outputFile.getParentFile().isFile()) {
                try {
                    throw new CommandValidatorException(Messages.i18n.format("GetOntology.InvalidOutputPath",
                            command._outputFile.getCanonicalPath()));
                } catch (IOException e) {
                    throw new CommandValidatorException(Messages.i18n.format("GetOntology.InvalidOutputPath"));
                }
            }
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
     * Gets the output file.
     *
     * @return the output file
     */
    public File getOutputFile() {
        return _outputFile;
    }

    /**
     * Sets the output file.
     *
     * @param outputFile
     *            the new output file
     */
    public void setOutputFile(File outputFile) {
        this._outputFile = outputFile;
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

}
