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
import org.jboss.aesh.cl.validator.CommandValidator;
import org.jboss.aesh.cl.validator.CommandValidatorException;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.overlord.sramp.atom.archive.SrampArchive;
import org.overlord.sramp.atom.archive.SrampArchiveEntry;
import org.overlord.sramp.common.SrampModelUtils;
import org.overlord.sramp.shell.ShellCommandConstants;
import org.overlord.sramp.shell.aesh.validator.EntryFileNameValidator;
import org.overlord.sramp.shell.api.InvalidCommandArgumentException;
import org.overlord.sramp.shell.api.ShellContext;
import org.overlord.sramp.shell.i18n.Messages;


/**
 * Removes an entry from the current S-RAMP batch archive.
 *
 * @author eric.wittmann@redhat.com
 */
@CommandDefinition(name = ShellCommandConstants.Archive.ARCHIVE_COMMAND_UPDATE_ENTRY, validator = UpdateEntryArchiveCommand.CustomValidator.class, description = "Removes an entry from the current S-RAMP batch archive.")
public class UpdateEntryArchiveCommand extends AbstractArchiveShellCommand {

    @Option(required = true, name = "path", hasValue = true, shortName = 'e')
    private String _path;

    @Option(required = true, name = "fileName", hasValue = true, shortName = 'f', validator = EntryFileNameValidator.class)
    private String _fileName;

    @Option(hasValue = false, name = "content", shortName = 'c')
    private boolean _content;

    @Option(hasValue = false, name = "property", shortName = 'p')
    private boolean _property;

    @Option(hasValue = false, name = "relationship", shortName = 'r')
    private boolean _relationship;

    @Option(overrideRequired = true, name = "help", hasValue = false, shortName = 'h')
    private boolean _help;

    /**
     * Constructor.
     */
    public UpdateEntryArchiveCommand() {
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
        String relativeEntryPath = getCompletePath(_path, _fileName);
        if (_content) { //$NON-NLS-1$
            executeSetContent(archive, relativeEntryPath, getContext());
        }
        if (_property) { //$NON-NLS-1$
            executeSetProperty(archive, relativeEntryPath, getContext());
        }
        if (_relationship) { //$NON-NLS-1$
            executeSetRelationship(archive, relativeEntryPath, getContext());
        }

        return true;
    }

    /**
     * Can set the content for an entry.
     *
     * @param archive
     *            the archive
     * @param entryPath
     *            the entry path
     * @param context
     *            the context
     * @throws Exception
     *             the exception
     */
    private void executeSetContent(SrampArchive archive, String entryPath, ShellContext context)
            throws Exception {
        String pathToContentArg = requiredArgument(2,
                Messages.i18n.format("UpdateEntry.InvalidArgMsg.MissingPath")); //$NON-NLS-1$
        File file = new File(pathToContentArg);
        if (!file.isFile()) {
            throw new InvalidCommandArgumentException(2, Messages.i18n.format(
                    "UpdateEntry.FileNotFound", pathToContentArg)); //$NON-NLS-1$
        }

        InputStream contentStream = null;
        try {
            contentStream = FileUtils.openInputStream(file);
            SrampArchiveEntry entry = archive.getEntry(entryPath);
            archive.updateEntry(entry, contentStream);
            print(Messages.i18n.format("UpdateEntry.SuccessMsg")); //$NON-NLS-1$
        } finally {
            IOUtils.closeQuietly(contentStream);
        }
    }

    /**
     * Can set a property (built-in or custom) on the entry.
     *
     * @param archive
     *            the archive
     * @param entryPath
     *            the entry path
     * @param context
     *            the context
     * @throws Exception
     *             the exception
     */
    private void executeSetProperty(SrampArchive archive, String entryPath, ShellContext context)
            throws Exception {
        String propNameArg = requiredArgument(2,
                Messages.i18n.format("UpdateEntry.InvalidArgMsg.PropertyName")); //$NON-NLS-1$
        String propValArg = requiredArgument(3,
                Messages.i18n.format("UpdateEntry.InvalidArgMsg.PropertyValue")); //$NON-NLS-1$

        SrampArchiveEntry entry = archive.getEntry(entryPath);
        BaseArtifactType metaData = entry.getMetaData();

        if ("name".equals(propNameArg)) { //$NON-NLS-1$
            metaData.setName(propValArg);
        } else if ("description".equals(propNameArg)) { //$NON-NLS-1$
            metaData.setDescription(propValArg);
        } else if ("version".equals(propNameArg)) { //$NON-NLS-1$
            metaData.setVersion(propValArg);
        } else if ("createdBy".equals(propNameArg)) { //$NON-NLS-1$
            metaData.setCreatedBy(propValArg);
        } else if ("lastModifiedBy".equals(propNameArg)) { //$NON-NLS-1$
            metaData.setLastModifiedBy(propValArg);
        } else if ("uuid".equals(propNameArg)) { //$NON-NLS-1$
            metaData.setUuid(propValArg);
        } else if ("createdTimestamp".equals(propNameArg)) { //$NON-NLS-1$
        } else if ("lastModifiedTimestamp".equals(propNameArg)) { //$NON-NLS-1$
        }

        SrampModelUtils.setCustomProperty(metaData, propNameArg, propValArg);
        archive.updateEntry(entry, null);
        print(Messages.i18n.format("UpdateEntry.MetaDataSuccessMsg")); //$NON-NLS-1$
    }

    /**
     * Can set a relationship on an entry.
     *
     * @param archive
     *            the archive
     * @param entryPath
     *            the entry path
     * @param context
     *            the context
     * @throws Exception
     *             the exception
     */
    private void executeSetRelationship(SrampArchive archive, String entryPath, ShellContext context)
            throws Exception {
        throw new InvalidCommandArgumentException(0,
                Messages.i18n.format("UpdateEntry.NotYetImplemented.Relationships")); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see org.overlord.sramp.shell.BuiltInShellCommand#getName()
     */
    @Override
    public String getName() {
        return ShellCommandConstants.Archive.ARCHIVE_COMMAND_UPDATE_ENTRY;
    }

    /**
     * Class validator that implements the aesh validator
     *
     * @author David Virgil Naranjo
     */
    public class CustomValidator implements CommandValidator<UpdateEntryArchiveCommand> {

        /**
         * Instantiates a new custom validator.
         */
        public CustomValidator() {

        }

        /* (non-Javadoc)
         * @see org.jboss.aesh.cl.validator.CommandValidator#validate(org.jboss.aesh.console.command.Command)
         */
        @Override
        public void validate(UpdateEntryArchiveCommand command) throws CommandValidatorException {
            if (archive == null) {
                print(Messages.i18n.format("NO_ARCHIVE_OPEN")); //$NON-NLS-1$
                throw new CommandValidatorException(Messages.i18n.format("Archive.No.Archive.Open"));
            }
            String relativeEntryPath = getCompletePath(_path, _fileName);

            if (!archive.containsEntry(relativeEntryPath)) {
                throw new CommandValidatorException(Messages.i18n.format(
                        "UpdateEntry.EntryNotFound", relativeEntryPath)); //$NON-NLS-1$
            }

            if (!_content && !_property && !_relationship) {
                throw new CommandValidatorException(
                        Messages.i18n.format("UpdateEntry.InvalidArgMsg.No.Subcommand"));
            } else if ((_content && _property) || (_content && _relationship) || (_property && _relationship)) {
                throw new CommandValidatorException(
                        Messages.i18n.format("UpdateEntry.InvalidArgMsg.Only.One.Action"));
            }

        }
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
     * Checks if is content.
     *
     * @return true, if is content
     */
    public boolean isContent() {
        return _content;
    }

    /**
     * Sets the content.
     *
     * @param content
     *            the new content
     */
    public void setContent(boolean content) {
        this._content = content;
    }

    /**
     * Checks if is property.
     *
     * @return true, if is property
     */
    public boolean isProperty() {
        return _property;
    }

    /**
     * Sets the property.
     *
     * @param property
     *            the new property
     */
    public void setProperty(boolean property) {
        this._property = property;
    }

    /**
     * Checks if is relationship.
     *
     * @return true, if is relationship
     */
    public boolean isRelationship() {
        return _relationship;
    }

    /**
     * Sets the relationship.
     *
     * @param relationship
     *            the new relationship
     */
    public void setRelationship(boolean relationship) {
        this._relationship = relationship;
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
