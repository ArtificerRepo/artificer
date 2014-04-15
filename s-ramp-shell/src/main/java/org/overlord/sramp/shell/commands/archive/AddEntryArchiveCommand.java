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
package org.overlord.sramp.shell.commands.archive;

import java.io.File;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.cl.Option;
import org.jboss.aesh.cl.completer.FileOptionCompleter;
import org.jboss.aesh.cl.completer.OptionCompleter;
import org.jboss.aesh.cl.converter.Converter;
import org.jboss.aesh.cl.validator.OptionValidatorException;
import org.jboss.aesh.console.command.completer.CompleterInvocation;
import org.jboss.aesh.console.command.converter.ConverterInvocation;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.common.ArtifactTypeEnum;
import org.overlord.sramp.shell.ShellCommandConstants;
import org.overlord.sramp.shell.aesh.validator.EntryFileNameValidator;
import org.overlord.sramp.shell.i18n.Messages;

/**
 * Adds an entry to the current S-RAMP batch archive.
 *
 * @author eric.wittmann@redhat.com
 */
@CommandDefinition(name = ShellCommandConstants.Archive.ARCHIVE_COMMAND_ADD_ENTRY, description = "Adds an entry to the current S-RAMP batch archive.")
public class AddEntryArchiveCommand extends AbstractArchiveShellCommand {

    @Option(required = true, name = "path", hasValue = true, shortName = 'p')
    private String _path;

    @Option(required = true, name = "fileName", hasValue = true, shortName = 'f', validator = EntryFileNameValidator.class)
    private String _fileName;

    @Option(required = true, name = "artifactType", hasValue = true, shortName = 't', converter = BaseArtifactTypeConverter.class, completer = ArtifactTypeCompleter.class)
    private BaseArtifactType _artifactType;

    @Option(required = false, name = "content", hasValue = true, shortName = 'c', completer = FileOptionCompleter.class)
    private File _content;

    @Option(overrideRequired = true, name = "help", hasValue = false, shortName = 'h')
    private boolean _help;

	/**
	 * Constructor.
	 */
	public AddEntryArchiveCommand() {
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

		if (archive == null) {
			print(Messages.i18n.format("NO_ARCHIVE_OPEN")); //$NON-NLS-1$
			return false;
		}
		InputStream contentStream = null;
		try {
            String name = _fileName;
			if (_content != null) {
				contentStream = FileUtils.openInputStream(_content);
			}
			_artifactType.setName(name);
            String relativePath = this.getCompletePath(_path, _fileName);
            archive.addEntry(relativePath, _artifactType, contentStream);
            print(Messages.i18n.format("AddEntry.Added", relativePath)); //$NON-NLS-1$
		} finally {
			IOUtils.closeQuietly(contentStream);
		}

		return true;
	}

    /*
     * (non-Javadoc)
     *
     * @see org.overlord.sramp.shell.BuiltInShellCommand#getName()
     */
    @Override
    public String getName() {
        return ShellCommandConstants.Archive.ARCHIVE_COMMAND_ADD_ENTRY;
    }

    /**
     * Converter class that converts an String to a BaseArtifactType
     *
     * @author David Virgil Naranjo
     */
    public class BaseArtifactTypeConverter implements Converter<BaseArtifactType, ConverterInvocation> {

        /**
         * Instantiates a new base artifact type converter.
         */
        public BaseArtifactTypeConverter() {

        }

        /*
         * (non-Javadoc)
         *
         * @see
         * org.jboss.aesh.cl.converter.Converter#convert(org.jboss.aesh.console
         * .command.converter.ConverterInvocation)
         */
        @Override
        public BaseArtifactType convert(ConverterInvocation converterInvocation)
                throws OptionValidatorException {
            ArtifactType type = ArtifactType.valueOf(converterInvocation.getInput());

            BaseArtifactType artifact = type.newArtifactInstance();

            return artifact;
        }
    }

    /**
     * Completer class that return all the results that match with an Artifact
     * Type
     *
     * @author David Virgil Naranjo
     */
    public class ArtifactTypeCompleter implements OptionCompleter<CompleterInvocation> {

        /**
         * Instantiates a new artifact type completer.
         */
        public ArtifactTypeCompleter() {

        }

        /*
         * (non-Javadoc)
         *
         * @see
         * org.jboss.aesh.cl.completer.OptionCompleter#complete(org.jboss.aesh
         * .console.command.completer.CompleterInvocation)
         */
        @Override
        public void complete(CompleterInvocation completerInvocation) {
            String artifact = completerInvocation.getGivenCompleteValue();
            for (ArtifactTypeEnum t : ArtifactTypeEnum.values()) {
                String candidate = t.getType();
                if (candidate.startsWith(artifact)) {
                    completerInvocation.addCompleterValue(candidate);
                }
            }
        }

    }


    /**
     * Gets the artifact.
     *
     * @return the artifact
     */
    public BaseArtifactType getArtifact() {
        return _artifactType;
    }

    /**
     * Sets the artifact.
     *
     * @param artifact
     *            the new artifact
     */
    public void setArtifact(BaseArtifactType artifact) {
        this._artifactType = artifact;
    }

    /**
     * Gets the content.
     *
     * @return the content
     */
    public File getContent() {
        return _content;
    }

    /**
     * Sets the content.
     *
     * @param content
     *            the new content
     */
    public void setContent(File content) {
        this._content = content;
    }

    /*
     * (non-Javadoc)
     *
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
     * Gets the path.
     *
     * @return the path
     */
    public String getPath() {
        return _path;
    }

    /**
     * Sets the path.
     *
     * @param path
     *            the new path
     */
    public void setPath(String path) {
        this._path = path;
    }

    /**
     * Gets the file name.
     *
     * @return the file name
     */
    public String getFileName() {
        return _fileName;
    }

    /**
     * Sets the file name.
     *
     * @param fileName
     *            the new file name
     */
    public void setFileName(String fileName) {
        this._fileName = fileName;
    }

    /**
     * Gets the artifact type.
     *
     * @return the artifact type
     */
    public BaseArtifactType getArtifactType() {
        return _artifactType;
    }

    /**
     * Sets the artifact type.
     *
     * @param artifactType
     *            the new artifact type
     */
    public void setArtifactType(BaseArtifactType artifactType) {
        this._artifactType = artifactType;
    }




}
