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

import javax.xml.namespace.QName;

import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.cl.Option;
import org.jboss.aesh.cl.completer.OptionCompleter;
import org.jboss.aesh.console.command.completer.CompleterInvocation;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.common.ArtifactTypeEnum;
import org.overlord.sramp.shell.ShellCommandConstants;
import org.overlord.sramp.shell.aesh.RequiredOptionRenderer;
import org.overlord.sramp.shell.aesh.converter.ExtendedArtifactTypeConverter;
import org.overlord.sramp.shell.api.InvalidCommandArgumentException;
import org.overlord.sramp.shell.i18n.Messages;


/**
 * This CLI command is used to create a new artifact in the repository.  This
 * command creates non-Document style artifacts (artifacts with no content).
 * It complements the {@link UploadArtifactCommand}, which creates a new
 * artifact *with* content.
 *
 * @author David Virgil Naranjo
 */
@CommandDefinition(name = ShellCommandConstants.Sramp.S_RAMP_COMMAND_CREATE, description = "This CLI command is used to create a new artifact in the repository.")
public class CreateArtifactCommand extends AbstractCoreShellCommand {



    @Option(required = true, name = "artifactType", hasValue = true, shortName = 't', converter = ExtendedArtifactTypeConverter.class, completer = ArtifactTypeCompleter.class, renderer = RequiredOptionRenderer.class)
    private ArtifactType _artifactType;

    @Option(required = true, name = "name", hasValue = true, shortName = 'n', renderer = RequiredOptionRenderer.class)
    private String _artifactName;

    @Option(hasValue = true, name = "description", shortName = 'd')
    private String _description;

    @Option(overrideRequired = true, name = "help", hasValue = false, shortName = 'h')
    private boolean _help;

    /**
     * Constructor.
     */
    public CreateArtifactCommand() {

    }

    /**
     * Execute.
     *
     * @return true, if successful
     * @throws Exception
     *             the exception
     * @see org.overlord.sramp.shell.api.ShellCommand#execute()
     */
    @Override
    public boolean execute() throws Exception {
        super.execute();
        if (client == null) {
            print(Messages.i18n.format("MissingSRAMPConnection")); //$NON-NLS-1$
        }

        if (_artifactType.isDerived()) {
            throw new InvalidCommandArgumentException(0, Messages.i18n.format("ArtifactModel.isDerived")); //$NON-NLS-1$
        } else if (_artifactType.isDocument()) {
            throw new InvalidCommandArgumentException(0, Messages.i18n.format("ArtifactModel.isDocument"));
        } else {
            BaseArtifactType artifact = _artifactType.newArtifactInstance();
            artifact.setName(_artifactName);
            artifact.setDescription(_description);
            artifact = client.createArtifact(artifact);
            // Put the artifact in the session as the active artifact
            QName artifactVarName = new QName("s-ramp", "artifact"); //$NON-NLS-1$ //$NON-NLS-2$
            getContext().setVariable(artifactVarName, artifact);
        }

        print(Messages.i18n.format("CreateArtifactCommand.Successful", _artifactName)); //$NON-NLS-1$
        return true;
    }

    /* (non-Javadoc)
     * @see org.overlord.sramp.shell.BuiltInShellCommand#getName()
     */
    @Override
    public String getName() {
        return ShellCommandConstants.Sramp.S_RAMP_COMMAND_CREATE;
    }

    /**
     * Completes the input string with the list of artifact types that match the
     * input.
     *
     * @author David Virgil Naranjo
     */
    private class ArtifactTypeCompleter implements OptionCompleter<CompleterInvocation> {

        /* (non-Javadoc)
         * @see org.jboss.aesh.cl.completer.OptionCompleter#complete(org.jboss.aesh.console.command.completer.CompleterInvocation)
         */
        @Override
        public void complete(CompleterInvocation completerInvocation) {
            String artifact = completerInvocation.getGivenCompleteValue();
            for (ArtifactTypeEnum t : ArtifactTypeEnum.values()) {
                String candidate = t.getType();
                if (artifact == null || candidate.startsWith(artifact) && t.isDocument() && !t.isDerived()) {
                    completerInvocation.addCompleterValue(candidate);
                }
            }
        }

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

    /**
     * Gets the description.
     *
     * @return the description
     */
    public String getDescription() {
        return _description;
    }

    /**
     * Sets the description.
     *
     * @param description
     *            the new description
     */
    public void setDescription(String description) {
        this._description = description;
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
     * Sets the artifact name.
     *
     * @param artifactName
     *            the new name
     */
    public void setArtifactName(String artifactName) {
        this._artifactName = artifactName;
    }

    /**
     * Gets the artifact name.
     *
     * @return the description
     */
    public String getArtifactName() {
        return _artifactName;
    }
}
