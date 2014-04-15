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
package org.overlord.sramp.shell.commands.core;

import java.io.File;
import java.io.InputStream;

import javax.xml.namespace.QName;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.cl.Option;
import org.jboss.aesh.cl.completer.FileOptionCompleter;
import org.jboss.aesh.cl.completer.OptionCompleter;
import org.jboss.aesh.cl.validator.CommandValidator;
import org.jboss.aesh.cl.validator.CommandValidatorException;
import org.jboss.aesh.console.command.completer.CompleterInvocation;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.overlord.sramp.atom.archive.SrampArchive;
import org.overlord.sramp.atom.archive.expand.DefaultMetaDataFactory;
import org.overlord.sramp.atom.archive.expand.ZipToSrampArchive;
import org.overlord.sramp.atom.archive.expand.registry.ZipToSrampArchiveRegistry;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.common.ArtifactTypeEnum;
import org.overlord.sramp.common.visitors.ArtifactVisitorHelper;
import org.overlord.sramp.shell.ShellCommandConstants;
import org.overlord.sramp.shell.aesh.converter.ExtendedDocumentArtifactTypeConverter;
import org.overlord.sramp.shell.i18n.Messages;
import org.overlord.sramp.shell.util.PrintArtifactMetaDataVisitor;


/**
 * Uploads an artifact to the s-ramp repository.
 *
 * @author eric.wittmann@redhat.com
 */
@CommandDefinition(name = ShellCommandConstants.Sramp.S_RAMP_COMMAND_UPLOAD, validator = UploadArtifactCommand.CustomCommandValidator.class, description = "Upload an artifact to s-ramp")
public class UploadArtifactCommand extends AbstractCoreShellCommand {

    @Option(hasValue = true, required = true, name = "file", shortName = 'f', completer = FileOptionCompleter.class)
    private File file;

    @Option(hasValue = true, shortName = 't', name = "artifactType", converter = ExtendedDocumentArtifactTypeConverter.class, completer = DocumentArtifactTypeCompleter.class)
    private ArtifactType _artifactType;

    @Option(overrideRequired = true, name = "help", hasValue = false, shortName = 'h')
    private boolean _help;
	/**
	 * Constructor.
	 */
	public UploadArtifactCommand() {
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

        InputStream content = null;
        ZipToSrampArchive expander = null;
        SrampArchive archive = null;
        if (client == null) {
            print(Messages.i18n.format("MissingSRAMPConnection")); //$NON-NLS-1$
        }
		try {
			content = FileUtils.openInputStream(file);
			BaseArtifactType artifact = client.uploadArtifact(_artifactType, content, file.getName());
            IOUtils.closeQuietly(content);

            // Now also add "expanded" content to the s-ramp repository
            expander = ZipToSrampArchiveRegistry.createExpander(_artifactType, file);
            if (expander != null) {
                expander.setContextParam(DefaultMetaDataFactory.PARENT_UUID, artifact.getUuid());
                archive = expander.createSrampArchive();
                client.uploadBatch(archive);
            }

			// Put the artifact in the session as the active artifact
			QName artifactVarName = new QName("s-ramp", "artifact"); //$NON-NLS-1$ //$NON-NLS-2$
			getContext().setVariable(artifactVarName, artifact);
			print(Messages.i18n.format("Upload.Success")); //$NON-NLS-1$
			PrintArtifactMetaDataVisitor visitor = new PrintArtifactMetaDataVisitor();
			ArtifactVisitorHelper.visitArtifact(visitor, artifact);
		} catch (Exception e) {
			print(Messages.i18n.format("Upload.Failure")); //$NON-NLS-1$
			print("\t" + e.getMessage()); //$NON-NLS-1$
			IOUtils.closeQuietly(content);
            return false;
		}
        return true;
	}

	/**
     * Try to figure out what kind of artifact we're dealing with.
     *
     * @param file
     *            the file
     * @return the artifact type
     */
	private ArtifactType determineArtifactType(File file) {
		ArtifactType type = null;
		String extension = FilenameUtils.getExtension(file.getName());
		if ("wsdl".equals(extension)) { //$NON-NLS-1$
			type = ArtifactType.WsdlDocument();
		} else if ("xsd".equals(extension)) { //$NON-NLS-1$
			type = ArtifactType.XsdDocument();
		} else if ("wspolicy".equals(extension)) { //$NON-NLS-1$
			type = ArtifactType.PolicyDocument();
		} else if ("xml".equals(extension)) { //$NON-NLS-1$
			type = ArtifactType.XmlDocument();
		} else {
			type = ArtifactType.Document();
		}
		return type;
	}


    /* (non-Javadoc)
     * @see org.overlord.sramp.shell.BuiltInShellCommand#getName()
     */
    @Override
    public String getName() {
        return ShellCommandConstants.Sramp.S_RAMP_COMMAND_UPLOAD;
    }

    /**
     * Validates that the Upload Artifact command is properly filled.
     *
     * @author David Virgil Naranjo
     */
    public class CustomCommandValidator implements CommandValidator<UploadArtifactCommand> {

        /**
         * Instantiates a new custom command validator.
         */
        public CustomCommandValidator() {

        }

        /* (non-Javadoc)
         * @see org.jboss.aesh.cl.validator.CommandValidator#validate(org.jboss.aesh.console.command.Command)
         */
        @Override
        public void validate(UploadArtifactCommand command) throws CommandValidatorException {
            if (command._artifactType == null) {
                command._artifactType = determineArtifactType(command.file);
            }
            if (command._artifactType == null) {
                throw new CommandValidatorException(
                        Messages.i18n.format("UploadArtifact.InvalidFileExtension"));

            }

        }
    }

    /**
     * Completes the input string with the list of document artifact types that
     * match the input.
     *
     * @author David Virgil Naranjo
     */
    private class DocumentArtifactTypeCompleter implements OptionCompleter<CompleterInvocation> {

        /* (non-Javadoc)
         * @see org.jboss.aesh.cl.completer.OptionCompleter#complete(org.jboss.aesh.console.command.completer.CompleterInvocation)
         */
        @Override
        public void complete(CompleterInvocation completerInvocation) {
            String artifact = completerInvocation.getGivenCompleteValue();
            for (ArtifactTypeEnum t : ArtifactTypeEnum.values()) {
                String candidate = t.getType();
                if (artifact == null || candidate.startsWith(artifact) && t.isDocument()) {
                    completerInvocation.addCompleterValue(candidate);
                }
            }
        }

    }

    /**
     * Gets the file.
     *
     * @return the file
     */
    public File getFile() {
        return file;
    }

    /**
     * Sets the file.
     *
     * @param file
     *            the new file
     */
    public void setFile(File file) {
        this.file = file;
    }

    /**
     * Gets the artifact type.
     *
     * @return the artifact type
     */
    public ArtifactType getArtifactType() {
        return _artifactType;
    }

    /**
     * Sets the artifact type.
     *
     * @param artifactType
     *            the new artifact type
     */
    public void setArtifactType(ArtifactType artifactType) {
        this._artifactType = artifactType;
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
