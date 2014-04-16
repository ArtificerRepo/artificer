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
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.cl.Option;
import org.jboss.aesh.cl.completer.FileOptionCompleter;
import org.jboss.aesh.cl.validator.CommandValidator;
import org.jboss.aesh.cl.validator.CommandValidatorException;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.shell.ShellCommandConstants;
import org.overlord.sramp.shell.aesh.validator.FileDirectoryValidator;
import org.overlord.sramp.shell.i18n.Messages;


/**
 * Gets the content for a single artifact in the s-ramp repo.
 *
 * @author eric.wittmann@redhat.com
 */
@CommandDefinition(name = ShellCommandConstants.Sramp.S_RAMP_COMMAND_GET_CONTENT, validator = GetContentCommand.CustomCommandValidator.class, description = "Gets the content for a single artifact in the s-ramp repo.")
public class GetContentCommand extends AbstractCoreShellCommand {



    @Option(required = true, hasValue = true, name = "outputDirectory", shortName = 'd', completer = FileOptionCompleter.class, validator = FileDirectoryValidator.class)
    private File _outputDirectory;

    @Option(required = false, hasValue = true, name = "feedIndex", shortName = 'f')
    private Integer _feedIndex;

    @Option(required = false, hasValue = true, name = "uuid", shortName = 'u')
    private String _uuid;

    @Option(overrideRequired = true, hasValue = false, name = "help", shortName = 'h')
    private boolean _help;

    private BaseArtifactType _artifact;
	/**
	 * Constructor.
	 */
	public GetContentCommand() {
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
        _artifact = getArtifact(_feedIndex, _uuid);
        String fileName = _artifact.getName();
        _outputDirectory = new File(_outputDirectory, fileName);

        if (_outputDirectory.getParentFile() != null)
            _outputDirectory.getParentFile().mkdirs();

		InputStream artifactContent = null;
		OutputStream outputStream = null;

		try {
			artifactContent = client.getArtifactContent(ArtifactType.valueOf(_artifact), _artifact.getUuid());
            outputStream = new FileOutputStream(_outputDirectory);
			IOUtils.copy(artifactContent, outputStream);
            print(Messages.i18n.format("GetContent.ContentSaved", _outputDirectory.getCanonicalPath())); //$NON-NLS-1$
		} finally {
			IOUtils.closeQuietly(artifactContent);
			IOUtils.closeQuietly(outputStream);
		}
        return true;
	}

    /* (non-Javadoc)
     * @see org.overlord.sramp.shell.BuiltInShellCommand#getName()
     */
    @Override
    public String getName() {
        return ShellCommandConstants.Sramp.S_RAMP_COMMAND_GET_CONTENT;
    }


    /**
     * Validates that the Get Content command is properly filled.
     * 
     * @author David Virgil Naranjo
     */
    public class CustomCommandValidator implements CommandValidator<GetContentCommand> {

        /**
         * Instantiates a new custom command validator.
         */
        public CustomCommandValidator() {

        }

        /* (non-Javadoc)
         * @see org.jboss.aesh.cl.validator.CommandValidator#validate(org.jboss.aesh.console.command.Command)
         */
        @Override
        public void validate(GetContentCommand command) throws CommandValidatorException {
            if (StringUtils.isBlank(command.getUuid()) && command.getFeedIndex() == null) {
                throw new CommandValidatorException(Messages.i18n.format("Artifact.feed.no.option.selected"));

            } else if (!StringUtils.isBlank(command.getUuid()) && command.getFeedIndex() != null) {
                throw new CommandValidatorException(
                        Messages.i18n.format("Artifact.feed.both.option.selected"));
            }

        }
    }

    /**
     * Gets the output file.
     *
     * @return the output file
     */
    public File getOutputFile() {
        return _outputDirectory;
    }

    /**
     * Sets the output file.
     *
     * @param outputFile
     *            the new output file
     */
    public void setOutputFile(File outputFile) {
        this._outputDirectory = outputFile;
    }

    /**
     * Gets the feed index.
     *
     * @return the feed index
     */
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

    /**
     * Gets the uuid.
     *
     * @return the uuid
     */
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

    /* (non-Javadoc)
     * @see org.overlord.sramp.shell.commands.core.AbstractCoreShellCommand#getArtifact()
     */
    @Override
    public BaseArtifactType getArtifact() {
        return _artifact;
    }

    /**
     * Sets the artifact.
     *
     * @param artifact
     *            the new artifact
     */
    public void setArtifact(BaseArtifactType artifact) {
        this._artifact = artifact;
    }

    /**
     * Gets the output directory.
     *
     * @return the output directory
     */
    public File getOutputDirectory() {
        return _outputDirectory;
    }

    /**
     * Sets the output directory.
     *
     * @param outputDirectory
     *            the new output directory
     */
    public void setOutputDirectory(File outputDirectory) {
        this._outputDirectory = outputDirectory;
    }

}
