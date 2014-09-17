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

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.overlord.sramp.client.SrampAtomApiClient;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.shell.BuiltInShellCommand;
import org.overlord.sramp.shell.api.InvalidCommandArgumentException;
import org.overlord.sramp.shell.i18n.Messages;

/**
 * This CLI command is used to create a new artifact in the repository. This
 * command creates non-Document style artifacts (artifacts with no content). It
 * complements the {@link UploadArtifactCommand}, which creates a new artifact
 * *with* content.
 * 
 * @author David Virgil Naranjo
 */
public class CreateArtifactCommand extends BuiltInShellCommand {

    private SrampAtomApiClient client;

    /**
     * Constructor.
     */
    public CreateArtifactCommand() {
    }

    /**
     * @see org.overlord.sramp.shell.api.ShellCommand#execute()
     */
    @Override
    public boolean execute() throws Exception {
        String artifactTypeArg = this.requiredArgument(0, Messages.i18n.format("ArtifactModel.Mandatory")); //$NON-NLS-1$
        String nameArg = this.requiredArgument(1, Messages.i18n.format("ArtifactName.Mandatory")); //$NON-NLS-1$
        String descriptionArg = this.optionalArgument(2);

        QName clientVarName = new QName("s-ramp", "client"); //$NON-NLS-1$ //$NON-NLS-2$
        client = (SrampAtomApiClient) getContext().getVariable(clientVarName);
        if (client == null) {
            print(Messages.i18n.format("MissingSRAMPConnection")); //$NON-NLS-1$
            return false;
        }

        ArtifactType artifactType = ArtifactType.valueOf(artifactTypeArg);
        if (artifactType.isExtendedType()) {
            artifactType = ArtifactType.ExtendedArtifactType(artifactType.getExtendedType(), false);
        }

        if (artifactType.isDerived()) {
            throw new InvalidCommandArgumentException(0, Messages.i18n.format("ArtifactModel.isDerived")); //$NON-NLS-1$
        } else if (artifactType.isDocument()) {
            throw new InvalidCommandArgumentException(0, Messages.i18n.format("ArtifactModel.isDocument")); //$NON-NLS-1$
        } else {
            BaseArtifactType artifact = artifactType.newArtifactInstance();
            artifact.setName(nameArg);
            artifact.setDescription(descriptionArg);
            artifact = client.createArtifact(artifact);
            // Put the artifact in the session as the active artifact
            QName artifactVarName = new QName("s-ramp", "artifact"); //$NON-NLS-1$ //$NON-NLS-2$
            getContext().setVariable(artifactVarName, artifact);
        }

        print(Messages.i18n.format("CreateArtifactCommand.Successful", nameArg)); //$NON-NLS-1$
        return true;
    }

}
