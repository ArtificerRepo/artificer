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

import javax.xml.namespace.QName;

import org.apache.commons.lang.StringUtils;
import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.cl.Option;
import org.jboss.aesh.cl.completer.FileOptionCompleter;
import org.jboss.aesh.cl.validator.CommandValidator;
import org.jboss.aesh.cl.validator.CommandValidatorException;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.overlord.sramp.atom.archive.SrampArchiveJaxbUtils;
import org.overlord.sramp.common.visitors.ArtifactVisitorHelper;
import org.overlord.sramp.shell.ShellCommandConstants;
import org.overlord.sramp.shell.aesh.validator.FileDirectoryValidator;
import org.overlord.sramp.shell.i18n.Messages;
import org.overlord.sramp.shell.util.PrintArtifactMetaDataVisitor;


/**
 * Gets the full meta-data for a single artifact in the s-ramp repo.
 *
 * @author eric.wittmann@redhat.com
 */
@CommandDefinition(name = ShellCommandConstants.Sramp.S_RAMP_COMMAND_GET_METADATA, validator = GetMetaDataCommand.CustomCommandValidator.class, description = "Gets the full meta-data for a single artifact in the s-ramp repo.")
public class GetMetaDataCommand extends AbstractCoreShellCommand {



    @Option(hasValue = true, name = "", shortName = 'd', completer = FileOptionCompleter.class, validator = FileDirectoryValidator.class)
    private File _outputDirectory;


    private BaseArtifactType _artifact;

    @Option(required = false, name = "feedIndex", hasValue = true, shortName = 'f')
    private Integer _feedIndex;

    @Option(required = false, name = "uuid", hasValue = true, shortName = 'u')
    private String _uuid;

    @Option(overrideRequired = true, name = "help", hasValue = false, shortName = 'h')
    private boolean _help;

	/**
	 * Constructor.
	 */
	public GetMetaDataCommand() {
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

		// Store the artifact in the context, making it the active artifact.
		QName artifactVarName = new QName("s-ramp", "artifact"); //$NON-NLS-1$ //$NON-NLS-2$
		getContext().setVariable(artifactVarName, _artifact);

		if (_outputDirectory == null) {
			// Print out the meta-data information
			print(Messages.i18n.format("GetMetaData.MetaDataLabel", _artifact.getUuid())); //$NON-NLS-1$
			print("--------------"); //$NON-NLS-1$
			PrintArtifactMetaDataVisitor visitor = new PrintArtifactMetaDataVisitor();
			ArtifactVisitorHelper.visitArtifact(visitor, _artifact);
		} else {
            String fileName = _artifact.getName() + "-metadata.xml"; //$NON-NLS-1$
            _outputDirectory = new File(_outputDirectory, fileName);

            _outputDirectory.getParentFile().mkdirs();
			SrampArchiveJaxbUtils.writeMetaData(_outputDirectory, _artifact, false);
			print(Messages.i18n.format("GetMetaData.SavedTo", _outputDirectory.getCanonicalPath())); //$NON-NLS-1$
		}
        return true;
	}


    /* (non-Javadoc)
     * @see org.overlord.sramp.shell.BuiltInShellCommand#getName()
     */
    @Override
    public String getName() {
        return ShellCommandConstants.Sramp.S_RAMP_COMMAND_GET_METADATA;
    }


    /**
     * Validates that the Get MetaData command is properly filled.
     * 
     * @author David Virgil Naranjo
     */
    public class CustomCommandValidator implements CommandValidator<GetMetaDataCommand> {

        /**
         * Instantiates a new custom command validator.
         */
        public CustomCommandValidator() {

        }

        /* (non-Javadoc)
         * @see org.jboss.aesh.cl.validator.CommandValidator#validate(org.jboss.aesh.console.command.Command)
         */
        @Override
        public void validate(GetMetaDataCommand command) throws CommandValidatorException {
            if (StringUtils.isBlank(command.getUuid()) && command.getFeedIndex() == null) {
                throw new CommandValidatorException(Messages.i18n.format("Artifact.feed.no.option.selected"));

            } else if (!StringUtils.isBlank(command.getUuid()) && command.getFeedIndex() != null) {
                throw new CommandValidatorException(
                        Messages.i18n.format("Artifact.feed.both.option.selected"));
            }

        }
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

}
